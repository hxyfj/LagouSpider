package spider;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import com.google.gson.Gson;

import dao.LagouDao;
import entity.Company;
import entity.CompanyResult;
import util.HttpUtil;

/**
 * 抓取公司信息
 */
public class CompanySpider {

	// 线程数
	private static final int NTHREADS = 10;

	private BlockingQueue<Company> companysQueue = new LinkedBlockingQueue<>();;

	private ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);
	// 当前爬取的公司数
	private int count;
	// 若公司信息已经全部抓取到队列当中,则为true
	private volatile boolean flag;

	public static void main(String[] args) {
		// 创建爬虫
		new CompanySpider();
	}

	public CompanySpider() {
		System.out.println("-------------拉勾爬虫开始工作-------------");
		// 不断从队列中读取公司信息保存到数据库当中
		saveCompanys();
		// 抓取公司信息保存到队列当中
		getCompanys();
		// 爬取结束
		executorService.shutdown();
		while (!executorService.isTerminated()) {
		}
		System.out.println("-------------拉勾爬虫工作结束-------------");
	}

	private void saveCompanys() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				SqlSession session = getSessionFactory().openSession();
				LagouDao dao = session.getMapper(LagouDao.class);
				while (true) {
					try {
						// 如果队列为空且flag为true代表公司已抓取完毕
						if (companysQueue.size() == 0 && flag) {
							break;
						}
						Company company = companysQueue.take();

						// 公司不存在则添加
						if (dao.getCompany(company.getCompanyId()) == null) {
							dao.addCompany(company);
							session.commit();
							synchronized (this) {
								++count;
								System.out.println("第" + count + "个公司信息添加成功!");
							}
						} else {
							synchronized (this) {
								++count;
								System.out.println("第" + count + "个公司信息添加失败：该公司信息已存在!");
							}
						}

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		for (int i = 0; i < NTHREADS; i++) {
			executorService.execute(runnable);
		}
	}

	private void getCompanys() {
		Map<String, String> params = new HashMap<>();
		params.put("first", "false");
		params.put("sortField", "0");
		params.put("havemark", "0");
		// 页码
		int pn = 1;
		// 当前页含有的公司数目
		int pageSize = 0;
		do {
			params.put("pn", "" + pn++);
			String data = HttpUtil.post("http://www.lagou.com/gongsi/0-0-0.json", params);
			// 数据解析
			Gson gson = new Gson();
			CompanyResult companyResult = gson.fromJson(data, CompanyResult.class);

			pageSize = companyResult.getPageSize();
			// 公司信息
			List<Company> companys = companyResult.getResult();
			// 将公司信息添加到队列当中
			for (int i = 0; i < companys.size(); i++) {
				companysQueue.add(companys.get(i));
			}
		} while (pageSize == 16);
		// 抓取完毕
		flag = true;
	}

	/**
	 * 生成session工厂
	 */
	private SqlSessionFactory getSessionFactory() {
		String resource = "conf.xml";
		InputStream is = CompanySpider.class.getClassLoader().getResourceAsStream(resource);
		return new SqlSessionFactoryBuilder().build(is);
	}

}

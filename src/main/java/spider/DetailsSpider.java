package spider;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import dao.LagouDao;
import util.HttpUtil;

/**
 * 进一步抓取职位描述和工作地点
 */
public class DetailsSpider {

	// 线程数
	private static final int NTHREADS = 10;

	private ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);

	List<Integer> positionIds;

	public static void main(String[] args) {
		// 创建爬虫
		new DetailsSpider();
	}

	public DetailsSpider() {
		System.out.println("拉勾爬虫开始工作");
		// 获取需要补充详细信息的职位
		getPositionIds();
		// 保存详细信息
		saveDetails();
	}

	private void saveDetails() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				SqlSession session = getSessionFactory().openSession();
				LagouDao dao = session.getMapper(LagouDao.class);
				int size;
				int positionId;
				while (true) {
					synchronized (positionIds) {
						size = positionIds.size();
						if (size == 0) {
							break;
						} else {
							positionId = positionIds.remove(0);
						}
						System.out.println("职位id为" + positionId + "的信息即将开始更新!——目前剩余" + (size - 1) + "个未更新的职位");
					}
					getDetails(positionId, session, dao);
				}
			}
		};
		for (int i = 0; i < NTHREADS; i++) {
			executorService.execute(runnable);
		}
		executorService.shutdown();
		while (!executorService.isTerminated()) {
		}
		System.out.println("拉勾爬虫工作结束");
	}

	private void getPositionIds() {
		SqlSession session = getSessionFactory().openSession();
		try {
			LagouDao dao = session.getMapper(LagouDao.class);
			positionIds = dao.getPositionIds();
			if (positionIds.size() == 0) {
				System.out.println("职位信息已经全部补全完毕");
			}
		} finally {
			session.close();
		}
	}

	private void getDetails(int positionId, SqlSession session, LagouDao dao) {
		String url = "http://www.lagou.com/jobs/" + positionId + ".html";
		String html = HttpUtil.get(url);
		// 对应职位存在
		if (html != null) {
			Document doc = Jsoup.parse(html);
			Element descLink = doc.select("dd.job_bt").first();
			Element addrLink = doc.select("dl.job_company > dd > div").first();
			// 防止空指针异常
			if (descLink != null && addrLink != null) {
				String positionDescription = descLink.text();
				String positionAddress = addrLink.text();
				dao.updatePosition(positionId, positionDescription, positionAddress);
			}
		} else { // 对应职位信息已被删除
			// 删除数据库中对应的信息
			dao.deletePosition(positionId);
			System.out.println("id为" + positionId + "的职位信息不存在");
		}
		session.commit();

	}

	/**
	 * 生成session工厂
	 */
	private SqlSessionFactory getSessionFactory() {
		String resource = "conf.xml";
		InputStream is = DetailsSpider.class.getClassLoader().getResourceAsStream(resource);
		return new SqlSessionFactoryBuilder().build(is);
	}
}

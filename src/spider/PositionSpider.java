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
import entity.JsonResult;
import entity.Position;
import entity.PositionResult;
import util.HttpUtil;

/**
 * 抓取搜索列表中的职位信息
 */
public class PositionSpider {

	private String city;

	private String keyword;
	// 线程数
	private static final int NTHREADS = 10;

	private BlockingQueue<Position> positionsQueue = new LinkedBlockingQueue<>();;

	private ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);
	// 当前爬取的职位数
	private int count;
	// 若职位信息已经全部抓取到队列当中,则为true
	private volatile boolean flag;

	public static void main(String[] args) {
		// 创建爬虫
		new PositionSpider("杭州", "Java");
	}

	public PositionSpider(String city, String keyword) {
		this.city = city;
		this.keyword = keyword;

		System.out.println("-------------针对" + city + keyword + "的拉勾爬虫开始工作-------------");
		// 不断从队列中读取职位信息保存到数据库当中
		savePositions();
		// 抓取职位信息保存到队列当中
		getPositions();
		// 爬取结束
		executorService.shutdown();
		while (!executorService.isTerminated()) {
		}
		System.out.println("-------------针对" + city + keyword + "的拉勾爬虫工作结束-------------");
	}

	private void savePositions() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				SqlSession session = getSessionFactory().openSession();
				LagouDao dao = session.getMapper(LagouDao.class);
				while (true) {
					try {
						// 如果队列为空且flag为true代表职位已抓取完毕
						if (positionsQueue.size() == 0 && flag) {
							break;
						}
						Position position = positionsQueue.take();

						// 不存在则添加
						if (dao.getPosition(position.getPositionId()) == null) {
							dao.addPosition(position);
							session.commit();
							synchronized (this) {
								++count;
								System.out.println(city + keyword + "的第" + count + "个职位信息添加成功!");
							}
						} else {
							synchronized (this) {
								++count;
								System.out.println(city + keyword + "的第" + count + "个职位信息添加失败：该职位信息已存在!");
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

	private void getPositions() {
		Map<String, String> params = new HashMap<>();
		params.put("first", "true");
		params.put("kd", keyword);
		// 页码
		int pn = 1;
		// 当前页含有的职位数目
		int pageSize = 0;
		do {
			params.put("pn", "" + pn++);
			String data = HttpUtil.post("http://www.lagou.com/jobs/positionAjax.json?px=default&city=" + city, params);
			// 数据解析
			Gson gson = new Gson();
			JsonResult jsonResult = gson.fromJson(data, JsonResult.class);
			// 解析后的数据对象
			PositionResult positionResult = jsonResult.getContent().getPositionResult();
			pageSize = positionResult.getPageSize();
			// 职位信息
			List<Position> positions = positionResult.getResult();
			// 将职位信息添加到队列当中
			for (int i = 0; i < positions.size(); i++) {
				positionsQueue.add(positions.get(i));
			}
		} while (pageSize == 15);
		// 抓取完毕
		flag = true;
	}

	/*
	 * 生成session工厂
	 */
	private SqlSessionFactory getSessionFactory() {
		String resource = "conf.xml";
		InputStream is = PositionSpider.class.getClassLoader().getResourceAsStream(resource);
		return new SqlSessionFactoryBuilder().build(is);
	}
}

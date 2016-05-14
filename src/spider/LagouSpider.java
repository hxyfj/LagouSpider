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
public class LagouSpider {
	
	private String city = "杭州";
	
	private String keyword = "Java";

	private BlockingQueue<Position> positionsQueue = new LinkedBlockingQueue<>();;

	private ExecutorService executorService = Executors.newFixedThreadPool(10);
	// 当前爬取的职位数
	private int count;

	public static void main(String[] args) {
		// 创建爬虫
		new LagouSpider();
	}

	public LagouSpider() {
		System.out.println("拉勾爬虫开始工作");
		// 不断从队列中读取职位信息保存到数据库当中
		savePositions();
		// 抓取职位信息保存到队列当中
		getPositions();
	}

	private void savePositions() {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				SqlSession session = getSessionFactory().openSession();
				LagouDao dao = session.getMapper(LagouDao.class);
				while (true) {
					try {
						Position position = positionsQueue.take();

						// 不存在则添加
						if (dao.getPosition(position.getPositionId()) == null) {
							dao.addPosition(position);
							session.commit();	
							synchronized (this) {
								++count;
								System.out.println("第" + count + "个职位信息添加成功!");
							}
						} else {
							synchronized (this) {
								++count;
								System.out.println("第" + count + "个职位信息添加失败：该职位信息已存在!");
							}
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		for (int i = 0; i < 10; i++) {
			executorService.execute(runnable);
		}
		while (!executorService.isTerminated()) {  
		}  
		System.out.println("拉勾爬虫工作结束");  
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
	}

	/*
	 * 生成session工厂
	 */
	private SqlSessionFactory getSessionFactory() {
		String resource = "conf.xml";
		InputStream is = LagouSpider.class.getClassLoader().getResourceAsStream(resource);
		return new SqlSessionFactoryBuilder().build(is);
	}
}

package spider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import dao.LagouDao;
import entity.JsonResult;
import entity.Position;
import entity.PositionResult;
import util.HttpUtil;

/**
 * 抓取职位信息
 */
public class PositionSpider {

	// 线程数
	private static final int NTHREADS = 20;

	private BlockingQueue<Position> positionsQueue = new LinkedBlockingQueue<>();;

	private ExecutorService executorService = Executors.newFixedThreadPool(NTHREADS);

	// 当前爬取的职位数
	private int count;

	// 若职位信息已经全部抓取到队列当中,则为true
	private volatile boolean flag;

	// 所有城市列表
	private List<String> citys = new ArrayList<>();

	// 所有技术类型列表
	private List<String> types = new ArrayList<>();

	public static void main(String[] args) {
		// 创建爬虫
		// 因为拉勾网最多只返回5000条信息，因此要根据关键字并按城市下面的行政区进行分类爬取,以便汇总
		 new PositionSpider("北京", "java");
		// 不设置参数爬取所有信息
//		new PositionSpider();
	}

	/**
	 * 爬取所有城市的技术类职位
	 */
	public PositionSpider() {
		System.out.println("-------------拉勾爬虫开始工作-------------");
		// 读取城市列表
		initCitys();
		// 读取职位类型列表
		initTypes();
		// 不断从队列中读取职位信息保存到数据库当中
		savePositions();
		// 抓取职位信息保存到队列当中(一次性爬取中途会出错,原因不明,通过控制循环下标可以从出错点重新开始爬取)
		for (int i = 0; i < types.size(); i++) {
			System.out.println(i + "-------------目前职业：" + types.get(i) + " -------------");
			for (int j = 0; j < citys.size(); j++) {
				System.out.println(citys.get(j));
				getPositions(citys.get(j), types.get(i));
			}
		}
		// 抓取完毕
		flag = true;
		// 爬取结束
		executorService.shutdown();
		while (!executorService.isTerminated()) {
		}
		System.out.println("-------------拉勾爬虫工作结束-------------");
	}

	private void initTypes() {
		String url = "http://www.lagou.com/";
		String html = HttpUtil.get(url);
		Document doc = Jsoup.parse(html);
		Element divLink = doc.select("div.mainNavs > div.menu_box").first();
		Elements links = divLink.select("div.menu_sub > dl > dd > a");
		for (Element link : links) {
			types.add(link.text());
		}
	}

	private void initCitys() {
		String url = "http://www.lagou.com/jobs/list_";
		String html = HttpUtil.get(url);
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("div.city-wrapper > a, div.more > li.other > a.more-city-name");
		for (Element link : links) {
			citys.add(link.text());
		}
	}

	/**
	 * 关键字爬取指定城市下的职位信息
	 */
	public PositionSpider(String city, String keyword) {

		System.out.println("-------------" + city + keyword + "拉勾爬虫开始工作-------------");
		// 不断从队列中读取职位信息保存到数据库当中
		savePositions();
		// 抓取职位信息保存到队列当中
		getPositions(city, keyword);
		// 爬取结束
		executorService.shutdown();
		while (!executorService.isTerminated()) {
		}
		System.out.println("-------------" + city + keyword + "拉勾爬虫工作结束-------------");
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
						Position position = positionsQueue.poll(2, TimeUnit.SECONDS);
						if (position == null) {
							continue;
						}
						// 职位不存在则添加
						if (dao.getPosition(position.getPositionId()) == null) {
							dao.addPosition(position);
							session.commit();
							synchronized (this) {
								++count;
								System.out.println(position.getCity() + " " + position.getPositionType() + " 第" + count + "个职位信息添加成功!");
							}
						} else {
							synchronized (this) {
								++count;
								System.out.println(
										position.getCity() + " " + position.getPositionType() + " 第" + count + "个职位信息添加失败：该职位信息已存在!");
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

	private void getPositions(String city, String keyword) {
		// 获取该城市下的行政区
		String url = "http://www.lagou.com/jobs/list_Java?px=default&city=" + city + "#filterBox";
		String html = HttpUtil.get(url);
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("li.detail-district-area > a");
		for (Element link : links) {
			String district = link.text();
			// 获取行政区下的职位
			if (!district.equals("不限")) {
				getPositionsInDistrict(city, district, keyword);
			}
		}
	}

	private void getPositionsInDistrict(String city, String district, String keyword) {
		Map<String, String> params = new HashMap<>();
		params.put("first", "true");
		params.put("kd", HttpUtil.parseStr(keyword));
		// 页码
		int pn = 1;
		// 当前页含有的职位数目
		int pageSize = 0;
		do {
			params.put("pn", "" + pn++);
			String data = HttpUtil.post("http://www.lagou.com/jobs/positionAjax.json?px=new&city=" + city + "&district=" + district, params);
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
				// 解析工资范围
				regrexSalary(positions.get(i));
				// 添加到队列
				positionsQueue.add(positions.get(i));
			}
		} while (pageSize == 15);
	}
	
	/**
	 * 通过正则表达式解析得到最高工资和最低工资
	 */
	private void regrexSalary(Position position){
		String salary = position.getSalary();
		Pattern pattern = null;

		if (salary.indexOf("以上") != -1) {
			pattern = Pattern.compile("([0-9]+)k()");
		} else if (salary.indexOf("以下") != -1) {
			pattern = Pattern.compile("()([0-9]+)k");
		} else {
			pattern = Pattern.compile("([0-9]+)k-([0-9]+)k");
		}		
		Matcher matcher = pattern.matcher(salary);
		if(matcher.find()){
			position.setSalaryMin(Integer.parseInt(matcher.group(1)));
			position.setSalaryMax(Integer.parseInt(matcher.group(2)));
		}
	}

	/**
	 * 生成session工厂
	 */
	private SqlSessionFactory getSessionFactory() {
		String resource = "conf.xml";
		InputStream is = PositionSpider.class.getClassLoader().getResourceAsStream(resource);
		return new SqlSessionFactoryBuilder().build(is);
	}

}

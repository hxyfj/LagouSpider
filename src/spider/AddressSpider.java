package spider;

import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;

import entity.JsonResult;
import entity.PositionResult;
import util.HttpUtil;

/**
 * 抓取指定城市中职位的地区分布情况
 */
public class AddressSpider {

	private String city = "杭州";

	private String keyword = "Java";

	public static void main(String[] args) {
		// 创建爬虫
		new AddressSpider();
	}

	public AddressSpider() {
		System.out.println("拉勾爬虫开始工作");
		System.out.println();
		// 爬取城市下的行政区
		getDistrict();
		System.out.println("拉勾爬虫工作结束");
	}

	private void getDistrict() {
		System.out.println("行政区职位数:");
		String url = "http://www.lagou.com/jobs/list_Java?px=default&city=" + city + "#filterBox";
		String html = HttpUtil.get(url);
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("li.detail-district-area > a");
		for (Element link : links) {
			String district = link.text();
			int count = getCount(district,"");
			// 只显示有职位的地区
			if (count != 0) {
				System.out.println(district + ":" + count);
				// 获取对应商区信息
				getBizArea(district);
			}
		}
		System.out.println("---------------------------------------------------------");
	}

	private void getBizArea(String district) {
		String url = "http://www.lagou.com/jobs/list_?px=default&city=" + city + "&district=" + district + "#filterBox";
		String html = HttpUtil.get(url);
		Document doc = Jsoup.parse(html);
		Elements links = doc.select("li.detail-bizArea-area > a");
		for (Element link : links) {
			String bizArea = link.text();
			int count = getCount(district, bizArea);
			// 只显示有职位的地区
			if (count != 0) {
				System.out.println("   " + bizArea + ":" + count);
			}
		}
		System.out.println("---------------------------------------------------------");
	}

	private int getCount(String district, String bizArea) {
		Map<String, String> params = new HashMap<>();
		params.put("first", "true");
		params.put("pn", "1");
		params.put("kd", keyword);

		String data = HttpUtil.post(
				"http://www.lagou.com/jobs/positionAjax.json?"
				+ "px=default&city=" + city + "&district=" + district + "&bizArea=" + bizArea + "&needAddtionalResult=false",
				params);
		// 数据解析
		Gson gson = new Gson();
		JsonResult jsonResult = gson.fromJson(data, JsonResult.class);
		// 解析后的数据对象
		PositionResult positionResult = jsonResult.getContent().getPositionResult();
		int totalCount = positionResult.getTotalCount();
		return totalCount;
	}

}

package entity;

public class JsonResult {
	private Content content;

	public class Content {
		private PositionResult positionResult;


		public PositionResult getPositionResult() {
			return positionResult;
		}

		public void setPositionResult(PositionResult positionResult) {
			this.positionResult = positionResult;
		}

	}

	public Content getContent() {
		return content;
	}

	public void setContent(Content content) {
		this.content = content;
	}

}

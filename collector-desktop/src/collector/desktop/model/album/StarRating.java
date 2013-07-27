package collector.desktop.model.album;

//import collector.desktop.model.database.utilities.DatabaseStringUtilities;


public enum StarRating {
	ZeroStars  (0),
	OneStar    (1),
	TwoStars   (2),
	ThreeStars (3),
	FourStars  (4),
	FiveStars  (5);
	
	private int numericValue;
	
	private StarRating(int numericValue) {
		this.numericValue = numericValue;
	}
	
	public int getIntegerValue() {
		return numericValue;
	} 

	public static String[] toArray() {
		return new String[] {
				StarRating.ZeroStars.toString(),
				StarRating.OneStar.toString(),
				StarRating.TwoStars.toString(),
				StarRating.ThreeStars.toString(),
				StarRating.FourStars.toString(),
				StarRating.FiveStars.toString()};
	}

	public static Object getByIntegerValue(int integerValue) {
		for (StarRating starRating : StarRating.values()) {
			if (starRating.getIntegerValue() == integerValue) {
				return starRating;
			}
		}
		
		return StarRating.ZeroStars;
	}
}

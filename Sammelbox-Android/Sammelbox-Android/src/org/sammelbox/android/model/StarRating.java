package org.sammelbox.android.model;

public enum StarRating {
	ZERO_STARS  (0),
	ONE_STAR    (1),
	TWO_STARS   (2),
	THREE_STARS (3),
	FOUR_STARS  (4),
	FIVE_STARS  (5);
	
	private int numericValue;
	
	private StarRating(int numericValue) {
		this.numericValue = numericValue;
	}
	
	public int getIntegerValue() {
		return numericValue;
	} 

	public static String[] toComboBoxArray() {
		return new String[] {
				String.valueOf(StarRating.ZERO_STARS.getIntegerValue()),
				String.valueOf(StarRating.ONE_STAR.getIntegerValue()),
				String.valueOf(StarRating.TWO_STARS.getIntegerValue()),
				String.valueOf(StarRating.THREE_STARS.getIntegerValue()),
				String.valueOf(StarRating.FOUR_STARS.getIntegerValue()),
				String.valueOf(StarRating.FIVE_STARS.getIntegerValue())};
	}

	public static Object getByIntegerValue(int integerValue) {
		for (StarRating starRating : StarRating.values()) {
			if (starRating.getIntegerValue() == integerValue) {
				return starRating;
			}
		}
		
		return StarRating.ZERO_STARS;
	}
}

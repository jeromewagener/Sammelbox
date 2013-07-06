package collector.desktop.model.album;

public enum StarRating {
	ZeroStars,
	OneStar,
	TwoStars,
	ThreeStars,
	FourStars,
	FiveStars;

	public static final String DBStartTag = "<!DBStarRatingColumnTag!>";

	public static String[] toArray() {
		return new String[] {
				StarRating.ZeroStars.toString(),
				StarRating.OneStar.toString(),
				StarRating.TwoStars.toString(),
				StarRating.ThreeStars.toString(),
				StarRating.FourStars.toString(),
				StarRating.FiveStars.toString()};
	}
}

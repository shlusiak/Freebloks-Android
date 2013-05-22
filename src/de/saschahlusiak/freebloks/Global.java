package de.saschahlusiak.freebloks;

public class Global {
	public final static String PACKAGE_NAME = "de.saschahlusiak.freebloks";
	
	public final static int VIBRATE_START_DRAGGING = 85;
	public final static int VIBRATE_SET_STONE = 65;
	public final static int VIBRATE_STONE_SNAP = 40;
	
	/* minimum number of starts before rating dialog appears */
	public final static int RATE_MIN_STARTS = 8;
	
	/* minimum elapsed time after first start, before rating dialog appears */
	public static final long RATE_MIN_ELAPSED = 4 * (24 * 60 * 60 * 1000);
	
	public final static String base64EncodedPublicKey =
			"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsV2nQN/odu41MVs9jWCUiFBYlEKh+s+NeT81970AJo1t/0o+n46sQhxBBRuPAGPKiUPH1QEWwM+JgfNdaHjAX66D2Y4KlpQRu/u3hJjnRzn0hYWMOyjhhP06Dr+CKNworbRGdAbvWcUtkxjDXdixYExfIvX5Kdt/84evRzFjW/9JgpTYbqPnOt6qo1cuJkRfGKADTGbjk2POLY/s+tlcYrNUJScNBDgjfSgrY1fDAbv6T0JY+HaDkQSFfnb+W+nNZ6N/1pLizTjAX9/A5iZVc058jrFV0utXXpAd9b/CtxjETF/WnfXBVmdue+glG4WlacIZMpq2x6r09pJ2HbbOsQIDAQAB";

	/* set to true for Amazon export */
	public final static boolean IS_AMAZON = false;
	
	
	public final static String getMarketURLString() {
		if (IS_AMAZON)
			return "http://www.amazon.com/gp/mas/dl/android?p=" + PACKAGE_NAME;
		else
			return "https://play.google.com/store/apps/details?id=" + PACKAGE_NAME;
	}
}

package collector.desktop.internationalization;


public class Tests {
	public static void main(String[] argv) {
		Translator translator = new Translator(Language.DE);
		
		System.out.println(translator.get(DictKeys.TEST_MSG, "1"));
		
		translator.changeLanguage(Language.EN);
		
		System.out.println(translator.get(DictKeys.TEST_MSG, "2"));
	}
}

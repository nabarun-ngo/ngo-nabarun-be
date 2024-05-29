package ngo.nabarun.app.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.IllegalRegexRule;
import org.passay.PasswordData;
import org.passay.PasswordGenerator;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;

public class PasswordUtils {
	private final static PasswordGenerator passwordGenerator = new PasswordGenerator();

	public static String generateStrongPassword(int length) {

		List<CharacterRule> rules = Arrays.asList(
				new CharacterRule(EnglishCharacterData.UpperCase, 1),
				new CharacterRule(EnglishCharacterData.LowerCase, 1), 
				new CharacterRule(EnglishCharacterData.Digit, 1),
				new CharacterRule(EnglishCharacterData.Special, 1));

		PasswordGenerator generator = new PasswordGenerator();
		String password = generator.generatePassword(length, rules);
		return password;
	}
	
	public static RuleResult validatePassword(String password,String regex) {
		Rule rule=new IllegalRegexRule(regex);
		PasswordValidator generator = new PasswordValidator(List.of(rule));
		RuleResult result = generator.validate(new PasswordData(password));
		return result;
	}
	
	public static String generateRandomPassword(int length, boolean allowDigits, boolean allowSpecialChar, boolean allowUppercase, boolean allowLowercase) {
        List<CharacterRule> ruleList = new ArrayList<>();
        ruleList.add(new CharacterRule(EnglishCharacterData.Alphabetical));
        if(allowDigits) {
            ruleList.add(new CharacterRule(EnglishCharacterData.Digit));
        }
        if(allowSpecialChar) {
        	CharacterRule specialCharacterRule = new CharacterRule(new CharacterData() {
        	    @Override
        	    public String getErrorCode() {
        	        return "INVALID_SPECIAL_CHARACTER";
        	    }

        	    @Override
        	    public String getCharacters() {
        	        return "@#$%^&*?";
        	    }
        	});
            ruleList.add(specialCharacterRule);
        }
        if(allowUppercase) {
            ruleList.add(new CharacterRule(EnglishCharacterData.UpperCase));
        }
        if(allowLowercase) {
            ruleList.add(new CharacterRule(EnglishCharacterData.LowerCase));
        }
		return passwordGenerator.generatePassword(length, ruleList);

    }
	
	public static String generateRandomString(int length, boolean alphaNumeric) {
        List<CharacterRule> ruleList = new ArrayList<>();
        ruleList.add(new CharacterRule(EnglishCharacterData.Alphabetical));
        if(alphaNumeric) {
            ruleList.add(new CharacterRule(EnglishCharacterData.Digit));
        }
		return passwordGenerator.generatePassword(length, ruleList);

    }
	
	
	
	public static String generateRandomNumber(int digits) {
        return passwordGenerator.generatePassword(digits, List.of(new CharacterRule(EnglishCharacterData.Digit)));

    }
	
	public static boolean isPasswordValid(String password,String regex) {
        return validatePassword(password, regex).isValid();

    }
	
	
}

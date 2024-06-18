package nl.abcbank.identity.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;

import static org.passay.DigestDictionaryRule.ERROR_CODE;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordGenerator {

    private static final String ALLOWED_SPECIAL_CHARS = "!@#$%^&*()_+";

    private static final int NUMBER_OF_CHARS_FOR_LOWER_CASE_CHARS = 5;
    private static final int NUMBER_OF_CHARS_FOR_UPPER_CASE_CHARS = 5;
    private static final int NUMBER_OF_CHARS_FOR_DIGITS = 4;
    private static final int NUMBER_OF_CHARS_FOR_SPECIAL_CHARS = 2;
    private static final int PASSWORD_LENGTH = NUMBER_OF_CHARS_FOR_LOWER_CASE_CHARS +
            NUMBER_OF_CHARS_FOR_UPPER_CASE_CHARS +
            NUMBER_OF_CHARS_FOR_DIGITS +
            NUMBER_OF_CHARS_FOR_SPECIAL_CHARS;

    public static String generatePassword() {
        org.passay.PasswordGenerator gen = new org.passay.PasswordGenerator();
        CharacterData lowerCaseChars = EnglishCharacterData.LowerCase;
        CharacterRule lowerCaseRule = new CharacterRule(lowerCaseChars);
        lowerCaseRule.setNumberOfCharacters(NUMBER_OF_CHARS_FOR_LOWER_CASE_CHARS);

        CharacterData upperCaseChars = EnglishCharacterData.UpperCase;
        CharacterRule upperCaseRule = new CharacterRule(upperCaseChars);
        upperCaseRule.setNumberOfCharacters(NUMBER_OF_CHARS_FOR_UPPER_CASE_CHARS);

        CharacterData digitChars = EnglishCharacterData.Digit;
        CharacterRule digitRule = new CharacterRule(digitChars);
        digitRule.setNumberOfCharacters(NUMBER_OF_CHARS_FOR_DIGITS);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return ERROR_CODE;
            }

            public String getCharacters() {
                return ALLOWED_SPECIAL_CHARS;
            }
        };

        CharacterRule specialCharRule = new CharacterRule(specialChars);
        specialCharRule.setNumberOfCharacters(NUMBER_OF_CHARS_FOR_SPECIAL_CHARS);

        return gen.generatePassword(PASSWORD_LENGTH, specialCharRule, lowerCaseRule, upperCaseRule, digitRule);
    }

}

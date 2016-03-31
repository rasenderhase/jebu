package de.nikem.jebu.util.junit;

import org.mockito.Mockito;
import org.mockito.verification.VerificationMode;

public class MockitoTool {

	/**
	 * Wrap {@link Mockito#verify(Object, VerificationMode)} into assert-named method to satisfy PMD (it does not recognize
	 * verify as assertion)
	 * @param mock
	 * @param mode
	 * @return
	 * @see <a href="https://pmd.github.io/pmd-5.4.1/pmd-java/rules/java/junit.html#JUnitTestsShouldIncludeAssert">PMD Rule JUnitTestsShouldIncludeAssert</a>
	 */
	public static <T> T assertBevaviour(T mock, VerificationMode mode) {
        return Mockito.verify(mock, mode);
    }
}

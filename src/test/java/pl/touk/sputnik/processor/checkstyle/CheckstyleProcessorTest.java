package pl.touk.sputnik.processor.checkstyle;

import org.junit.Test;
import org.mockito.Mock;
import pl.touk.sputnik.TestEnvironment;
import pl.touk.sputnik.configuration.ConfigurationBuilder;
import pl.touk.sputnik.review.Review;
import pl.touk.sputnik.review.ReviewException;
import pl.touk.sputnik.review.ReviewResult;

import java.io.File;
import java.util.Properties;

import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionAssertJ.then;
import static com.googlecode.catchexception.apis.CatchExceptionAssertJ.when;
import static org.assertj.core.api.Assertions.assertThat;

public class CheckstyleProcessorTest extends TestEnvironment {

    private CheckstyleProcessor fixture;

    @Mock
    private Review review;

    @Test
    public void shouldReturnBasicSunViolationsOnSimpleClass() {
        // given
        fixture = new CheckstyleProcessor(config);

        // when
        ReviewResult reviewResult = fixture.process(review());

        // then
        assertThat(reviewResult).isNotNull();
        assertThat(reviewResult.getViolations())
                .isNotEmpty()
                .hasSize(3)
                .extracting("message")
                .containsOnly(
                        "Missing package-info.java file.",
                        "Missing a Javadoc comment."
                );
    }

    @Test
    public void shouldReturnViolationsOfChecksParametrizedWithProperties() {
        // given
        Properties properties = new Properties();
        properties.put("checkstyle.enabled", "true");
        properties.put("checkstyle.configurationFile", "src/test/resources/checkstyle/checks_with_properties.xml");
        properties.put("checkstyle.propertiesFile", "src/test/resources/checkstyle/checkstyle.properties");
        fixture = new CheckstyleProcessor(ConfigurationBuilder.initFromProperties(properties));

        // when
        ReviewResult reviewResult = fixture.process(review());

        // then
        assertThat(reviewResult).isNotNull();
        assertThat(reviewResult.getViolations())
                .extracting("message")
                .containsOnly(
                        "File length is 5 lines (max allowed is 3)."
                );
    }

    @Test
    public void shouldThrowReviewExceptionOnMissingPropertiesFile() {
        // given
        Properties properties = new Properties();
        properties.put("checkstyle.enabled", "true");
        properties.put("checkstyle.configurationFile", "src/test/resources/checkstyle/checks_with_properties.xml");
        properties.put("checkstyle.propertiesFile", "not_found_checkstyle.properties");
        fixture = new CheckstyleProcessor(ConfigurationBuilder.initFromProperties(properties));

        when(fixture).process(review());

        then(caughtException()).isInstanceOf(ReviewException.class)
                .hasMessage("IO exception when reading Checkstyle properties.");
    }

}

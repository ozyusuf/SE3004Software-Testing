package shopeasy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Task 1 - Specification-Based Testing (Chapter 2).
 *
 * Domain testing on PriceCalculator.calculate(base, discount, tax).
 * Three input dimensions, each split into equivalence classes:
 *   - basePrice:    {0}, (0, large), very-large
 *   - discountRate: {0}, (0, 100), {100}
 *   - taxRate:      {0}, (0, 100), {100}
 *
 * On-point boundaries are 0 and 100 for the rates. A few off-point tests at
 * the bottom of the file cover the invalid-input partition required by the
 * spec; they overlap on purpose with ContractTest because contracts only
 * kick in once asserts are added in Task 3.
 */
class PriceCalculatorSpecTest {

    private static final double EPS = 0.001;

    private PriceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PriceCalculator();
    }

    // Partition: basePrice = 0 - result must be 0 regardless of the other rates.
    @Test
    void zeroBasePrice_alwaysReturnsZero() {
        assertThat(calculator.calculate(0, 25, 18)).isCloseTo(0.0, within(EPS));
        assertThat(calculator.calculate(0, 0, 0)).isCloseTo(0.0, within(EPS));
        assertThat(calculator.calculate(0, 100, 100)).isCloseTo(0.0, within(EPS));
    }

    // Identity: 0% discount AND 0% tax -> result equals basePrice.
    @Test
    void zeroDiscountAndZeroTax_returnsBasePrice() {
        double result = calculator.calculate(120.0, 0, 0);
        assertThat(result).isCloseTo(120.0, within(EPS));
    }

    // Boundary: discountRate lower bound (0%) - no reduction.
    @Test
    void discountAtLowerBound_zeroPercent_keepsPrice() {
        double result = calculator.calculate(50.0, 0, 10);
        assertThat(result).isCloseTo(55.0, within(EPS));
    }

    // Boundary: discountRate upper bound (100%) - everything wiped, tax on zero is zero.
    @Test
    void discountAtUpperBound_hundredPercent_zerosResult() {
        double result = calculator.calculate(200.0, 100, 25);
        assertThat(result).isCloseTo(0.0, within(EPS));
    }

    // Boundary: taxRate lower bound (0%) - discounted price returned as-is.
    @Test
    void taxAtLowerBound_zeroPercent_noTaxAdded() {
        double result = calculator.calculate(80.0, 10, 0);
        assertThat(result).isCloseTo(72.0, within(EPS));
    }

    // Boundary: taxRate upper bound (100%) - effectively doubles the discounted price.
    @Test
    void taxAtUpperBound_hundredPercent_doublesDiscountedPrice() {
        double result = calculator.calculate(100.0, 0, 100);
        assertThat(result).isCloseTo(200.0, within(EPS));
    }

    // Partition: typical mid-range values across all three dimensions.
    // base * (1 - d/100) * (1 + t/100) = expected
    @ParameterizedTest(name = "base={0}, disc={1}%, tax={2}% -> {3}")
    @CsvSource({
            "100.0, 10.0, 20.0, 108.0",   // mid disc, mid tax
            "200.0,  0.0, 10.0, 220.0",   // no disc, low tax
            "150.0, 50.0,  0.0,  75.0",   // half off, no tax
            "1000.0, 25.0, 8.0, 810.0",   // typical retail
            "49.99, 15.0, 18.0, 50.13"    // odd values, real-world-ish
    })
    void typicalValues(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(0.01));
    }

    // Partition: very large basePrice should still compute without overflow.
    @Test
    void veryLargeBasePrice_handledWithoutOverflow() {
        double result = calculator.calculate(1_000_000_000.0, 10, 5);
        // 1e9 * 0.9 * 1.05 = 945_000_000
        assertThat(result).isCloseTo(945_000_000.0, within(1.0));
    }

    // Mid-partition fractional discount.
    @Test
    void fractionalDiscount() {
        // 200 * (1 - 0.125) * (1 + 0) = 175
        double result = calculator.calculate(200.0, 12.5, 0);
        assertThat(result).isCloseTo(175.0, within(EPS));
    }

    // Mid-partition fractional tax (e.g. 8.25% sales tax).
    @Test
    void fractionalTax() {
        // 100 * 1 * 1.0825 = 108.25
        double result = calculator.calculate(100.0, 0, 8.25);
        assertThat(result).isCloseTo(108.25, within(EPS));
    }

    // Boundary matrix: extreme combinations of 0/100 for discount and tax.
    @ParameterizedTest(name = "base={0}, disc={1}, tax={2} -> {3}")
    @CsvSource({
            "100.0,   0.0,   0.0, 100.0",
            "100.0,   0.0, 100.0, 200.0",
            "100.0, 100.0,   0.0,   0.0",
            "100.0, 100.0, 100.0,   0.0"
    })
    void boundaryMatrix(double base, double disc, double tax, double expected) {
        assertThat(calculator.calculate(base, disc, tax)).isCloseTo(expected, within(EPS));
    }

    // Convenience method: applyDiscountOnly should match calculate(base, d, 0).
    @Test
    void applyDiscountOnly_matchesCalculateWithZeroTax() {
        double viaConvenience = calculator.applyDiscountOnly(250.0, 20);
        double viaCalculate = calculator.calculate(250.0, 20, 0);
        assertThat(viaConvenience).isCloseTo(viaCalculate, within(EPS));
        assertThat(viaConvenience).isCloseTo(200.0, within(EPS));
    }

    // Convenience method: applyTaxOnly should match calculate(base, 0, t).
    @Test
    void applyTaxOnly_matchesCalculateWithZeroDiscount() {
        double viaConvenience = calculator.applyTaxOnly(250.0, 18);
        double viaCalculate = calculator.calculate(250.0, 0, 18);
        assertThat(viaConvenience).isCloseTo(viaCalculate, within(EPS));
        assertThat(viaConvenience).isCloseTo(295.0, within(EPS));
    }

    // Off-point: basePrice just below 0 is an invalid input.
    @Test
    void invalidInput_negativeBase_isRejected() {
        assertThatThrownBy(() -> calculator.calculate(-0.01, 10, 10))
                .isInstanceOf(AssertionError.class);
    }

    // Off-point: discountRate just outside [0,100] in both directions.
    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 100.01, -5.0, 150.0})
    void invalidInput_discountOutsideRange_isRejected(double badDiscount) {
        assertThatThrownBy(() -> calculator.calculate(100, badDiscount, 0))
                .isInstanceOf(AssertionError.class);
    }

    // Off-point: taxRate just outside [0,100] in both directions.
    @ParameterizedTest
    @ValueSource(doubles = {-0.01, 100.01, -10.0, 250.0})
    void invalidInput_taxOutsideRange_isRejected(double badTax) {
        assertThatThrownBy(() -> calculator.calculate(100, 0, badTax))
                .isInstanceOf(AssertionError.class);
    }
}

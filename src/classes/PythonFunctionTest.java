package classes;

import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class PythonFunctionTest {

    @Test
    public void testExisting() {
        PythonFunction function = new PythonFunction("add");
        final Object evaluate = function.evaluate(1, 2);
        assertEquals(new BigInteger("3"), evaluate);
    }

    @Test
    public void testNew() {
        try {
            PythonFunction function = new PythonFunction("empty", "num1", "num2");
            assertEquals(BigInteger.ZERO, function.evaluate(4, 2));
        } catch (IOException e) {
            e.printStackTrace();
            fail("IO exception");
        }
    }

}
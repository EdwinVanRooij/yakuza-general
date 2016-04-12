package test.domain;

import main.domain.Register;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Edwin
 *         Created on 3/31/2016.
 */
public class RegisterTest {

    Register r;

    @Before
    public void setUp() throws Exception {
        r = new Register();
    }

    @Test
    public void getNewsItems() throws Exception {
        int actual = r.getNewsItems().size();
        int expected = 6;

        assertEquals(expected, actual);
    }

}
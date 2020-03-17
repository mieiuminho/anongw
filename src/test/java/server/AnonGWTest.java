package server;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class AnonGWTest {
    private AnonGW model;

    @Before
    public void setUp() {
        this.model = new AnonGW(42);
    }

    @Test
    public void getX() {
        assertEquals(42, this.model.getX());
    }

    @Test
    public void setX() {
        this.model.setX(90);
        assertEquals(90, this.model.getX());
    }
}

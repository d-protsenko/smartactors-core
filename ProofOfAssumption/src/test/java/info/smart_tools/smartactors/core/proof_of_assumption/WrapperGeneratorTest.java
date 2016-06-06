package info.smart_tools.smartactors.core.proof_of_assumption;

import org.junit.Test;


/**
 * Created by sevenbits on 6/6/16.
 */
public class WrapperGeneratorTest {

    @Test
    public void generateClass()
            throws Exception {
        Class<? extends TestInterface> a = WrapperGenerator.generate(TestInterface.class);
        TestInterface ins = a.newInstance();
        ins.getA();
        ins.setB(1, true, 'a');
    }
}

interface TestInterface {
    Integer getA();
    void setB(Integer a, Boolean c, char d);
}


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package in.myrpc.receiver;

import java.util.regex.Pattern;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author kguthrie
 */
public class ScriptEnvironmentTest extends TestCase {

    public ScriptEnvironmentTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testParse() throws Exception {
        new ScriptEnvironment("a = \"b\";");
        new ScriptEnvironment(""
                + "ghg = get(\"http://google.com\");\n"
                + "g = post(\"asdfas\", \"asdfasdf\") + new;\n"
                + "");

    }

    @Test
    public void testEval() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment("a = \"b\"; #comment");
        env.evaluate();

        Assert.assertEquals("b", env.getVariableValue("a"));
    }

    @Test
    public void testEval2() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment("a = \"b\" + \"v\";");
        env.evaluate();

        Assert.assertEquals("bv", env.getVariableValue("a"));
    }

    @Test
    public void testEval3() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(""
                + "# this is a comment\n"
                + "a = \"b\"; # so is this\n"
                + "b = a + \"a\";");
        env.evaluate();

        Assert.assertEquals("ba", env.getVariableValue("b"));
    }

    @Test
    public void testEval4() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(
                "a = \"\\\"\\\\b\\\"\"; #comment");
        env.evaluate();

        Assert.assertEquals("\"\\b\"", env.getVariableValue("a"));
    }

    @Test
    public void testRandom() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(""
                + "a = random(\"3\");");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertTrue(Pattern.matches("[a-zA-Z0-9]{3}",
                env.getVariableValue("a")));
    }

    @Test
    public void testRandom2() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(""
                + "a = random(\"5\") + \"69\";");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertTrue(Pattern.matches("[a-zA-Z0-9]{5}69",
                env.getVariableValue("a")));
    }

    @Test
    public void testRandom3() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(""
                + "a = random(\"100\", \"false\");");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertTrue(Pattern.matches("[a-zA-Z]{100}",
                env.getVariableValue("a")));
    }

    @Test
    public void testRandom4() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(""
                + "a = random(\"100\", \"false\", \"false\");");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertTrue(Pattern.matches("[a-z]{100}",
                env.getVariableValue("a")));
    }

    @Test
    public void testRandom5() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(""
                + "a = random(\"100\", \"true\", \"false\", \"false\");");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertTrue(Pattern.matches("[0-9]{100}",
                env.getVariableValue("a")));
    }

    @Test
    public void testRegex() throws Exception {
        ScriptEnvironment env = new ScriptEnvironment(""
                + "a = regex(\"a(b+)c\", \"abbbbbc\");");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertEquals("bbbbb", env.getVariableValue("a"));
    }

    @Test
    public void testGet() throws Exception {
        MockScriptEnvironment env = new MockScriptEnvironment(""
                //+ "url = \"u\" + \"r\" + \"l\";"
                + "a = get(\"url\", \"referer\");");
        env.evaluate();

        Assert.assertEquals(1, env.getCount);
        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertEquals("url referer", env.getVariableValue("a"));
    }

    @Test
    public void testGet2() throws Exception {
        ScriptEnvironment env = new MockScriptEnvironment(""
                + "a = get(\"url\", \"referer\", \"2\");");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertEquals("url referer 2", env.getVariableValue("a"));
    }

    @Test
    public void testPost() throws Exception {
        ScriptEnvironment env = new MockScriptEnvironment(""
                + "a = post(\"url\", \"referer\", \"content\");");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertEquals("url referer content", env.getVariableValue("a"));
    }

    @Test
    public void testToken() throws Exception {
        ScriptEnvironment env = new MockScriptEnvironment(""
                + "a = token();");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertEquals("token", env.getVariableValue("a"));
    }

    @Test
    public void testIf() throws Exception {
        ScriptEnvironment env = new MockScriptEnvironment(""
                + "a = if(unknownVariable, \"true\", \"false\");"
                + "b = if (\"\", \"true\", \"false\");"
                + "c = if (\"nonEmpty\", \"true\", \"false\");");
        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertNotNull(env.getVariableValue("b"));
        Assert.assertNotNull(env.getVariableValue("c"));
        Assert.assertEquals("false", env.getVariableValue("a"));
        Assert.assertEquals("false", env.getVariableValue("b"));
        Assert.assertEquals("true", env.getVariableValue("c"));
    }

    @Test
    public void testGotoLabel() throws Exception {
        ScriptEnvironment env = new MockScriptEnvironment(""
                + ""
                + "a = \"0\";"
                + ""
                + "goto(\"label\");"
                + ""
                + "b = \"somethingThatNeverHappens\";"
                + ""
                + "label(\"label\");"
                + ""
                + "b = if(b, \"\", \"nonEmpty\");"
                + "a = inc(a);"
                + ""
                + "goto(if(b, \"label\", \"\"));");

        env.evaluate();

        Assert.assertNotNull(env.getVariableValue("a"));
        Assert.assertNotNull(env.getVariableValue("b"));
        Assert.assertEquals("2", env.getVariableValue("a"));
        Assert.assertEquals("", env.getVariableValue("b"));
    }
}

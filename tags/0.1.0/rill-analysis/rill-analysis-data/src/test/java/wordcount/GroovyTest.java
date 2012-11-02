package wordcount;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.hadoop.hive.jdbc.HiveDriver;
import org.junit.Assert;
import org.junit.Test;

public class GroovyTest {
	
	@Test
	public void groovyAvailable() throws Exception {
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("groovy");

        // basic example
        System.out.println(engine.eval("(1..10).sum()"));

        // example showing scripting variables
        engine.put("first", "HELLO");
        engine.put("second", "world");
        System.out.println(engine.eval("first.toLowerCase() + second.toUpperCase()"));
    }
	
	@Test
	public void testHiveDriver() throws Exception {
		
		new HiveDriver();
		Assert.assertTrue(Pattern.matches("jdbc:hive://.*", "jdbc:hive://"));
		Assert.assertTrue(DriverManager.getDriver("jdbc:hive://") != null);
	}
}

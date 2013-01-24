/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hadoop.samples.wordcount;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.Mapper;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * WordCount startup class. Bootstraps the Spring container which deploys and
 * runs a Map-Reduce job.
 * <p/>
 * Accepts as optional parameters location of one (or multiple) application
 * contexts that will be used for configuring the Spring container. See the
 * reference documentation for more {@link http
 * ://static.springsource.org/spring/
 * docs/3.0.x/spring-framework-reference/html/resources.html information}.
 * 
 * Note that in most (if not all) managed environments writing such a class is
 * not needed as Spring already provides the required integration.
 * 
 * @see org.springframework.web.context.ContextLoaderListener
 * @author Costin Leau
 */
public class Main {

	private static final String[] CONFIGS = new String[] { "META-INF/spring/context.xml" };

	public static void main(String[] args) {
		String[] res = (args != null && args.length > 0 ? args : CONFIGS);
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(res);
		// shutdown the context along with the VM
		ctx.registerShutdownHook();
	}

	public static class Reduce extends MapReduceBase implements
			Reducer<Text, IntWritable, Text, IntWritable> {
		public void reduce(Text key, Iterator<IntWritable> values,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			int sum = 0;
			while (values.hasNext()) {
				sum += ((IntWritable) values.next()).get();
			}
			output.collect(key, new IntWritable(sum));
		}
	}

	public static class MapClass extends MapReduceBase implements
			Mapper<LongWritable, Text, Text, IntWritable> {
		private static final IntWritable one = new IntWritable(1);
		private Text word;

		public MapClass() {
			this.word = new Text();
		}

		public void map(LongWritable key, Text value,
				OutputCollector<Text, IntWritable> output, Reporter reporter)
				throws IOException {
			String line = value.toString();
			StringTokenizer itr = new StringTokenizer(line);
			while (itr.hasMoreTokens()) {
				this.word.set(itr.nextToken());
				output.collect(this.word, one);
			}
		}
	}
}

package biweekly.property.marshaller;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.io.json.JCalValue;
import biweekly.property.FreeBusy;
import biweekly.property.marshaller.Sensei.Check;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.Duration;
import biweekly.util.Period;

/*
 Copyright (c) 2013, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @author Michael Angstadt
 */
public class FreeBusyMarshallerTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final FreeBusyMarshaller marshaller = new FreeBusyMarshaller();
	private final Sensei<FreeBusy> sensei = new Sensei<FreeBusy>(marshaller);

	private final Date start;
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 43);
		c.set(Calendar.SECOND, 2);
		start = c.getTime();
	}
	private final String startStr = "20130611T124302Z";
	private final String startStrExt = "2013-06-11T12:43:02Z";

	private final Date end;
	{
		Calendar c = Calendar.getInstance();
		c.setTime(start);
		c.add(Calendar.HOUR, 2);
		end = c.getTime();
	}
	private final String endStr = "20130611T144302Z";
	private final String endStrExt = "2013-06-11T14:43:02Z";

	private final Duration duration = Duration.builder().hours(2).build();
	private final String durationStr = duration.toString();

	private final FreeBusy withMultiple = new FreeBusy();
	{
		withMultiple.addValue(start, end);
		withMultiple.addValue(start, duration);
	}
	private final FreeBusy withSingle = new FreeBusy();
	{
		withSingle.addValue(start, end);
	}
	private final FreeBusy empty = new FreeBusy();

	@Test
	public void writeText() {
		sensei.assertWriteText(withSingle).run(startStr + "/" + endStr);
		sensei.assertWriteText(withMultiple).run(startStr + "/" + endStr + "," + startStr + "/" + durationStr);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(startStr + "/" + endStr).run(hasSingle);
		sensei.assertParseText(startStr + "/" + endStr + "," + startStr + "/" + durationStr).run(hasMultiple);
		sensei.assertParseText(startStr + "/" + endStr + ",invalid/" + durationStr).warnings(1).run(hasSingle);
		sensei.assertParseText(startStr + "/" + endStr + "," + startStr + "/invalid").warnings(1).run(hasSingle);
		sensei.assertParseText(startStr + "/" + endStr + "," + startStr + "/").warnings(1).run(hasSingle);
		sensei.assertParseText(startStr + "/" + endStr + "," + startStr).warnings(1).run(hasSingle);
		sensei.assertParseText("").run(has());
	}

	@Test
	public void writeXml() {
		//@formatter:off
		sensei.assertWriteXml(withSingle).run(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>");
		
		sensei.assertWriteXml(withMultiple).run(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>");
		//@formatter:on

		sensei.assertWriteXml(empty).run("");
	}

	@Test
	public void parseXml() {
		//@formatter:off
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>"
		).run(hasSingle);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>"
		).run(hasMultiple);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>invalid</start>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>"
		).warnings(1).run(hasSingle);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startStrExt  + "</start>" +
			"<duration>invalid</duration>" +
		"</period>"
		).warnings(1).run(hasSingle);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startStrExt  + "</start>" +
			"<end>invalid</end>" +
		"</period>"
		).warnings(1).run(hasSingle);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<start>" + startStrExt  + "</start>" +
		"</period>"
		).warnings(1).run(hasSingle);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<end>" + endStrExt + "</end>" +
		"</period>"
		).warnings(1).run(hasSingle);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period>" +
			"<duration>" + durationStr + "</duration>" +
		"</period>"
		).warnings(1).run(hasSingle);
		
		sensei.assertParseXml(
		"<period>" +
			"<start>" + startStrExt + "</start>" +
			"<end>" + endStrExt + "</end>" +
		"</period>" +
		"<period/>"
		).warnings(1).run(hasSingle);
		
		sensei.assertParseXml("").cannotParse();
		//@formatter:on
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withSingle).run(JCalValue.multi(startStrExt + "/" + endStrExt));
		sensei.assertWriteJson(withMultiple).run(JCalValue.multi(startStrExt + "/" + endStrExt, startStrExt + "/" + durationStr));
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(JCalValue.multi(startStrExt + "/" + endStrExt)).run(hasSingle);
		sensei.assertParseJson(JCalValue.multi(startStrExt + "/" + endStrExt, startStrExt + "/" + durationStr)).run(hasMultiple);

		sensei.assertParseJson(JCalValue.multi(startStrExt + "/" + endStrExt, "invalid/" + durationStr)).warnings(1).run(hasSingle);
		sensei.assertParseJson(JCalValue.multi(startStrExt + "/" + endStrExt, startStrExt + "/invalid")).warnings(1).run(hasSingle);
		sensei.assertParseJson(JCalValue.multi(startStrExt + "/" + endStrExt, startStrExt + "/")).warnings(1).run(hasSingle);
		sensei.assertParseJson(JCalValue.multi(startStrExt + "/" + endStrExt, startStrExt)).warnings(1).run(hasSingle);
		sensei.assertParseJson("").warnings(1).run(has());
	}

	private final Check<FreeBusy> hasSingle = has(new Period(start, end));
	private final Check<FreeBusy> hasMultiple = has(new Period(start, end), new Period(start, duration));

	private Check<FreeBusy> has(final Period... periods) {
		return new Check<FreeBusy>() {
			public void check(FreeBusy property) {
				assertEquals(Arrays.asList(periods), property.getValues());
			}
		};
	}
}

package biweekly.component;

import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;

import java.util.Date;

import org.junit.Test;

import biweekly.property.Classification;
import biweekly.property.Created;
import biweekly.property.DateStart;
import biweekly.property.LastModified;
import biweekly.property.Organizer;
import biweekly.property.RecurrenceId;
import biweekly.property.RecurrenceRule;
import biweekly.property.Sequence;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Url;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.Frequency;

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
public class VJournalTest {
	@Test
	public void validate_required() {
		VJournal component = new VJournal();
		component.getProperties().clear();
		assertValidate(component).run(2, 2);
	}

	@Test
	public void validate_optional() {
		VJournal component = new VJournal();
		component.addProperty(Classification.confidential());
		component.addProperty(Classification.confidential());
		component.addProperty(new Created(new Date()));
		component.addProperty(new Created(new Date()));
		component.addProperty(new DateStart(new Date()));
		component.addProperty(new DateStart(new Date()));
		component.addProperty(new LastModified(new Date()));
		component.addProperty(new LastModified(new Date()));
		component.addProperty(new Organizer(""));
		component.addProperty(new Organizer(""));
		component.addProperty(new RecurrenceId(new Date()));
		component.addProperty(new RecurrenceId(new Date()));
		component.addProperty(new Sequence(1));
		component.addProperty(new Sequence(1));
		component.addProperty(Status.cancelled());
		component.addProperty(Status.cancelled());
		component.addProperty(new Summary(""));
		component.addProperty(new Summary(""));
		component.addProperty(new Url(""));
		component.addProperty(new Url(""));
		assertValidate(component).run(3, 3, 3, 3, 3, 3, 3, 3, 3, 3);
	}

	@Test
	public void validate_status() {
		VJournal component = new VJournal();
		component.setStatus(Status.tentative());
		assertValidate(component).run(13);
	}

	@Test
	public void validate_different_date_datatypes() {
		VJournal component = new VJournal();
		component.setDateStart(new DateStart(date("2000-01-01"), false));
		component.setRecurrenceId(new RecurrenceId(date("2000-01-01"), true));
		assertValidate(component).run(19);
	}

	@Test
	public void validate_time_in_rrule() {
		//@formatter:off
		Recurrence[] recurrences = {
			new Recurrence.Builder(Frequency.DAILY).byHour(1).build(),
			new Recurrence.Builder(Frequency.DAILY).byMinute(1).build(),
			new Recurrence.Builder(Frequency.DAILY).bySecond(1).build()
		};
		//@formatter:on
		for (Recurrence recurrence : recurrences) {
			VJournal component = new VJournal();
			component.setDateStart(new DateStart(date("2000-01-01"), false));
			component.setRecurrenceRule(recurrence);
			assertValidate(component).run(5);
		}
	}

	@Test
	public void validate_multiple_rrules() {
		VJournal component = new VJournal();
		component.setDateStart(new DateStart(date("2000-01-01"), false));
		component.addProperty(new RecurrenceRule(new Recurrence.Builder(Frequency.DAILY).build()));
		component.addProperty(new RecurrenceRule(new Recurrence.Builder(Frequency.DAILY).build()));
		assertValidate(component).run(6);
	}
}
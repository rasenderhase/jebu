package de.nikem.jebu.impl;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import de.nikem.jebu.api.Subscriber;

public class EventBusImplTest {

	private EventBusImpl subject;
	private Subscriber subscriber;
	private Subscriber otherSubscriber;

	@Before
	public void setUp() throws Exception {
		subject = new EventBusImpl();
		subscriber = mock(Subscriber.class);
		when(subscriber.getId()).thenReturn("SUBSCRIBER");
		otherSubscriber = mock(Subscriber.class);
		when(otherSubscriber.getId()).thenReturn("OTHER_SUBSCRIBER");
	}

	@Test
	public void testPublish() {
		subject.subscribe("test.event", subscriber);

		subject.publish("test.event", "Test data");
		verify(subscriber, times(1)).publish("test.event", "Test data");
	}

	@Test
	public void testPublishOtherEvent() {
		subject.subscribe("test.event", subscriber);

		subject.publish("other.test.event", "Test data");
		verify(subscriber, never()).publish(anyString(), any());
	}

	@Test
	public void testSubscribe() {
		subject.subscribe("test.event", subscriber);

		assertThat("subscriber map should contain key ”test.event”.",
				subject.getSubscriberMap().containsKey("test.event"), is(true));
		assertThat("subscriber map should contain one item in subscriber collection for key ”test.event”.",
				subject.getSubscriberMap().get("test.event").size(), is(1));
		assertThat("subscriber map should contain our subscriber in collection for key ”test.event”.",
				subject.getSubscriberMap().get("test.event").iterator().next(), is(subscriber));
	}

	@Test
	public void testUnsubscribeSubscriberByEventName() {
		subject.subscribe("test.event", subscriber);
		subject.subscribe("other.test.event", subscriber);
		subject.subscribe("other.test.event", otherSubscriber);
		subject.unsubscribe("other.test.event", subscriber);

		assertThat("subscriber map should contain subscriber in collection for key ”test.event”.",
				subject.getSubscriberMap().get("test.event").contains(subscriber), is(true));
		assertThat("subscriber map should contain otherSubscriber in collection for key ”other.test.event”.",
				subject.getSubscriberMap().get("other.test.event").contains(otherSubscriber), is(true));
		assertThat("subscriber map should not contain subscriber in collection for key ”other.test.event”.",
				subject.getSubscriberMap().get("other.test.event").contains(subscriber), is(false));
		
		subject.publish("other.test.event", "Test data");
		verify(subscriber, never()).publish(anyString(), any());
		verify(otherSubscriber, times(1)).publish("other.test.event", "Test data");
	}

	@Test
	public void testUnsubscribeSubscriber() {
		subject.subscribe("test.event", subscriber);
		subject.subscribe("other.test.event", subscriber);
		subject.subscribe("other.test.event", otherSubscriber);
		subject.unsubscribe(subscriber);
		
		assertThat("subscriber map should not contain collection for key ”test.event” at all.",
				subject.getSubscriberMap().get("test.event"), is((Collection<Subscriber>) null));
		assertThat("subscriber map should contain otherSubscriber in collection for key ”other.test.event”.",
				subject.getSubscriberMap().get("other.test.event").contains(otherSubscriber), is(true));
		assertThat("subscriber map should not contain subscriber in collection for key ”other.test.event”.",
				subject.getSubscriberMap().get("other.test.event").contains(subscriber), is(false));
		
		subject.publish("other.test.event", "Test data");
		verify(subscriber, never()).publish(anyString(), any());
		verify(otherSubscriber, times(1)).publish("other.test.event", "Test data");
	}

	@Test
	public void testHasSubscribersByEventName() {
		subject.subscribe("test.event", subscriber);
		subject.subscribe("other.test.event", subscriber);
		subject.subscribe("other.test.event", otherSubscriber);
		
		assertThat("there should be subscribers for event test.event", subject.hasSubscribers("test.event"), is(true));
		assertThat("there should be subscribers for event other.test.event", subject.hasSubscribers("test.event"), is(true));
		
		subject.unsubscribe(subscriber);
		
		assertThat("there should be no more subscribers for event test.event", subject.hasSubscribers("test.event"), is(false));
		assertThat("there should be subscribers for event other.test.event", subject.hasSubscribers("other.test.event"), is(true));
	}

	@Test
	public void testHasSubscribers() {
		subject.subscribe("test.event", subscriber);
		subject.subscribe("other.test.event", subscriber);
		subject.subscribe("other.test.event", otherSubscriber);
		
		assertThat("there should be subscribers", subject.hasSubscribers(), is(true));
		
		subject.unsubscribe(subscriber);
		subject.unsubscribe("other.test.event", otherSubscriber);
		
		assertThat("there should be no more subscribers", subject.hasSubscribers(), is(false));
	}

	@Test
	public void testGetSubscriberMapInitial() {
		assertThat("subscriber map should not be null", subject.getSubscriberMap(), is(not((Map<String, Collection<Subscriber>>) null)));
		assertThat("subscriber map should be empty", subject.getSubscriberMap().isEmpty(), is(true));
	}
	
	@Test
	public void testGetSubscriberMapSubscribed() {
		subject.subscribe("test.event", subscriber);
		assertThat("subscriber map should not be empty", subject.getSubscriberMap().isEmpty(), is(false));
		assertThat("subscriber map should not be empty", subject.getSubscriberMap().get("test.event").contains(subscriber), is(true));
	}
	
	@Test
	public void testGetSubscriberMapNotSubscribed() {
		assertThat("subscriber map should not be empty", subject.getSubscriberMap().get("test.event"), is((Collection<Subscriber>) null));
	}

	@Test
	public void testGetId() {
		assertThat("ID should not be null", subject.getId(), is(not((String) null)));
		assertThat("ID should not be empty", subject.getId().isEmpty(), is(false));
	}

}

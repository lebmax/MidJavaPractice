package ru.yandex.practicum.test;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;
import ru.yandex.practicum.sync.CounterTest;

@JCStressTest
@Outcome(id = "2", expect = Expect.ACCEPTABLE, desc = "Оба потока увеличили счетчик")
@Outcome(expect = Expect.FORBIDDEN, desc = "Потеря инкремента из-за гонки")
@State
public class SimpleCounterJCStressTest {
	private final CounterTest.SimpleCounter counter = new CounterTest.SimpleCounter();
	
	@Actor
	public void actor1() {
		counter.increment();
	}
	
	@Actor
	public void actor2() {
		counter.increment();
	}
	
	@Arbiter
	public void arbiter(I_Result r) {
		r.r1 = (int) counter.getValue();
	}
}

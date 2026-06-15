package ru.yandex.practicum.test;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Arbiter;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;
import org.openjdk.jcstress.infra.results.I_Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CyclicBarrier;

@JCStressTest
@Outcome(id = "3", expect = Expect.ACCEPTABLE, desc = "Все потоки прошли барьер и напечатали документ")
@State
public class PrinterPoolBarrierJCStressTest {
	
	private static final int PRINTER_COUNT = 3;
	private static final Logger log = LoggerFactory.getLogger(PrinterPoolBarrierJCStressTest.class);
	private final CyclicBarrier barrier = new CyclicBarrier(PRINTER_COUNT);
	private int printed = 0;
	
	@Actor
	public void actor1() {
		try {
			barrier.await();
			synchronized (this) {
				printed++;
			}
		} catch (Exception e) {
			log.error("e: ", e);
		}
	}
	
	@Actor
	public void actor2() {
		try {
			barrier.await();
			synchronized (this) {
				printed++;
			}
		} catch (Exception e) {
			log.error("e: ", e);
		}
	}
	
	@Actor
	public void actor3() {
		try {
			barrier.await();
			synchronized (this) {
				printed++;
			}
		} catch (Exception e) {
			log.error("e: ", e);
		}
	}
	
	@Arbiter
	public void arbiter(I_Result r) {
		r.r1 = printed;
	}}

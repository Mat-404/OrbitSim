

import java.awt.Color;
import java.math.*;


public class Orbit1{
	
	// Defining static variables to be used within calculations
	static MathContext mc = new MathContext(30);
	static BigDecimal delta_time = new BigDecimal("3600");
	static int number_steps = 24*365;
	static BigDecimal gravity_constant = new BigDecimal("6.674e-11");
	static BigDecimal radiusEarth = new BigDecimal("6378.1");
	
	
	// Custom Scientific Notation class
	static class sciNotation {
		sciNotation(double coefficient, int power){
			c = coefficient;
			p = power;
		}
		double c;
		int p;
		
		BigDecimal getValue() {return new BigDecimal(c*Math.pow(10, p));}
		void updateSci () {
			while (c < 1) {c = c*10;p=p-1;}
			while (c > 10) {c = c/10;p=p+1;}
		}
	}
	
	// Scientific Notation Operators
	static sciNotation multiplySci (sciNotation a, sciNotation b) {
		double temp = a.c*b.c;
		int temp2 = a.p+b.p;
		if (temp > 10) {temp = temp / 10; temp2++;}
		return new sciNotation(temp,temp2);
	}
	
	static sciNotation divideSci (sciNotation a, sciNotation b) {
		double temp = a.c/b.c;
		int temp2 = a.p-b.p;
		if (temp < 1) {temp = temp * 10;temp2--;}
		return new sciNotation(temp, temp2);
	}
	
	static sciNotation convertSci(BigDecimal a) {
		double coefficient=0;
		int power=0;
		double value = a.doubleValue();
		
		
		while (value > 10 ) {
			power++;
			value = value/10;
		}
		coefficient = value;
		return new sciNotation(coefficient, power);
		}
	
	
	// Custom Vector class
	static class Vector {
		Vector (BigDecimal a, BigDecimal b) {
			first = a;
			second = b;
		}
		BigDecimal first;
		BigDecimal second;
		BigDecimal getdistance() {
			BigDecimal a = first.pow(2).add(second.pow(2)).sqrt(mc);
			return a;
		}
		Vector unitVector() {
			BigDecimal d=getdistance(); 
			BigDecimal tempFirst=first.divide(d, mc); 
			BigDecimal tempSecond=second.divide(d, mc);
			return new Vector (tempFirst, tempSecond);}
	}
	
	// Custom Vector operations
	static Vector scalarMultiply(Vector a, BigDecimal b) {
		return new Vector(a.first.multiply(b), a.second.multiply(b) );
	}
	
	static Vector scalarDivide(Vector a, BigDecimal b) {
		return new Vector(a.first.divide(b, mc),a.second.divide(b, mc));
	}
	
	static Vector vectorAdd(Vector a, Vector b) {
		return new Vector(a.first.add(b.first),a.second.add(b.second));
	}
	
	// Custom Object class
	static class Object {
		Object (String n, BigDecimal m, Vector p, Vector v) {
			name = n;
			mass = m;
			position = p;
			velocity = v;
			force = new Vector(new BigDecimal("0.0"),new BigDecimal("0.0"));
		}
		String name;
		BigDecimal mass;
		Vector position;
		Vector velocity;
		Vector force;
		
	}
	
	public static final void main (String[] args) throws InterruptedException {
		Plot moonPlot = new Plot("Moon Orbit", -385405000.0, 385405000.0, 50000000, -385405000.0, 385405000.0, 50000000);
		moonPlot.pointSize = 2;
		
		
		// Create objects for simulation
		Object Earth = new Object("Earth", new BigDecimal("5.9722e24"), 
				new Vector(new BigDecimal("0.0"),new BigDecimal("0.0")), 
				new Vector (new BigDecimal("0.0"),new BigDecimal("-12.5")) );
		Object Moon = new Object("Moon", new BigDecimal("7.342e22"), 
				new Vector(new BigDecimal("384405000.0"),new BigDecimal("0.0")), 
				new Vector (new BigDecimal("0.0"),new BigDecimal("1022.0")) );
		Object Satellite = new Object("Sat", new BigDecimal("1000.0"), 
				new Vector(new BigDecimal("284405000.0"),new BigDecimal("0.0")), 
				new Vector (new BigDecimal("0.0"),new BigDecimal("800")) );
		
		
		/*
		File file = new File ("planetData.txt");
		Scanner sc = new Scanner (file);
		ArrayList<Object> a = new ArrayList<Object>();
		int lineCount = 0;
		while (sc.hasNextLine()) {
			if (lineCount > 0) {
				a.add(new Object(sc.next(),new BigDecimal(Double.parseDouble(sc.next())),
						new Vector(new BigDecimal(Double.parseDouble(sc.next())),new BigDecimal(Double.parseDouble(sc.next()))),
						new Vector(new BigDecimal(Double.parseDouble(sc.next())),new BigDecimal(Double.parseDouble(sc.next())))));
			} else {sc.nextLine();}
			lineCount++;
		}
		Object[] objects = new Object[lineCount-1];
		for (int i = 0; i < a.size(); i++) {
			objects[i] = a.get(i);
		}
		*/
		
		
		Object[] objects = {Earth, Moon, Satellite};
		
		for (int step = 0; step < number_steps; step++) {
			
			for (int i = 0; i < objects.length; i++) {
				
				for (int j = i + 1; j < objects.length; j++) {
					
					Vector v = new Vector(objects[j].position.first.subtract(objects[i].position.first),
							objects[j].position.second.subtract( objects[i].position.second));
					Vector direction = v.unitVector();
					BigDecimal temp = v.getdistance();
					
					
					sciNotation distance = convertSci(temp);
					sciNotation distSquared = multiplySci(distance, distance);
					sciNotation sciForce =
							multiplySci(convertSci(gravity_constant),
								divideSci(multiplySci(convertSci(objects[i].mass),
										convertSci(objects[j].mass)),
									distSquared
								)
							);
					sciForce.updateSci();
					
					BigDecimal distance_sq = temp.pow(2);
					
					BigDecimal force = (objects[i].mass.multiply(objects[j].mass).
							divide(distance_sq, mc)).multiply(gravity_constant);
					// System.out.printf("force by %s on %s: %6.3e N, %6.3e N\n",objects[i].name,objects[j].name,direction.first.multiply(force),direction.second.multiply(force));	
					
					objects[i].force = vectorAdd(objects[i].force, scalarMultiply(direction, force));
					objects[j].force = vectorAdd(objects[j].force,scalarMultiply(direction, force.multiply(new BigDecimal("-1"))));
					
				}
			}
			
			for (int i = 0; i < objects.length; i++) {
				Vector acceleration = scalarDivide(objects[i].force,objects[i].mass);
				Vector oldVelocity = objects[i].velocity;
				Vector newVelocity = (vectorAdd(oldVelocity, scalarMultiply(acceleration,delta_time)));
				Vector newPosition = (vectorAdd(objects[i].position, 
						scalarMultiply(newVelocity,delta_time)));
				objects[i].velocity=newVelocity;
				objects[i].position=newPosition;
			}
			
			// System.out.printf("Position of Earth: %f, %f\n", objects[0].position.first.doubleValue(), objects[0].position.second.doubleValue());
			
			moonPlot.setColor(Color.RED);
			moonPlot.addPoint(objects[1].position.first.doubleValue(), 
					objects[1].position.second.doubleValue());
			moonPlot.setColor(Color.BLUE);
			moonPlot.addPoint(objects[0].position.first.doubleValue(), 
					objects[0].position.second.divide(new BigDecimal(100)).doubleValue());
			moonPlot.setColor(Color.GREEN);
			moonPlot.addPoint(objects[2].position.first.doubleValue(), 
					objects[2].position.second.doubleValue());
			
			for (int i = 0; i < objects.length; i++) {
				objects[i].force.first=new BigDecimal(0);
				objects[i].force.second=new BigDecimal(0);
			}
			Thread.sleep(1);
			
		}
	}
}


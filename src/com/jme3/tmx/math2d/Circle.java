package com.jme3.tmx.math2d;

import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;

public class Circle implements Shape2D {
	public float x, y;
	public float radius;

	/** Constructs a new circle with all values set to zero */
	public Circle () {

	}

	/** Constructs a new circle with the given X and Y coordinates and the given radius.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param radius The radius of the circle */
	public Circle (float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	/** Constructs a new circle using a given {@link Vector2f} that contains the desired X and Y coordinates, and a given radius.
	 * 
	 * @param position The position {@link Vector2f}.
	 * @param radius The radius */
	public Circle (Vector2f position, float radius) {
		this.x = position.x;
		this.y = position.y;
		this.radius = radius;
	}

	/** Copy constructor
	 * 
	 * @param circle The circle to construct a copy of. */
	public Circle (Circle circle) {
		this.x = circle.x;
		this.y = circle.y;
		this.radius = circle.radius;
	}

	/** Creates a new {@link Circle} in terms of its center and a point on its edge.
	 * 
	 * @param center The center of the new circle
	 * @param edge Any point on the edge of the given circle */
	public Circle (Vector2f center, Vector2f edge) {
		this.x = center.x;
		this.y = center.y;
		this.radius = center.distance(edge);
	}

	/** Sets a new location and radius for this circle.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * @param radius Circle radius */
	public void set (float x, float y, float radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}

	/** Sets a new location and radius for this circle.
	 * 
	 * @param position Position {@link Vector2f} for this circle.
	 * @param radius Circle radius */
	public void set (Vector2f position, float radius) {
		this.x = position.x;
		this.y = position.y;
		this.radius = radius;
	}

	/** Sets a new location and radius for this circle, based upon another circle.
	 * 
	 * @param circle The circle to copy the position and radius of. */
	public void set (Circle circle) {
		this.x = circle.x;
		this.y = circle.y;
		this.radius = circle.radius;
	}

	/** Sets this {@link Circle}'s values in terms of its center and a point on its edge.
	 * 
	 * @param center The new center of the circle
	 * @param edge Any point on the edge of the given circle */
	public void set (Vector2f center, Vector2f edge) {
		this.x = center.x;
		this.y = center.y;
		this.radius = center.distance(edge);
	}

	/** Sets the x and y-coordinates of circle center from vector
	 * @param position The position vector */
	public void setPosition (Vector2f position) {
		this.x = position.x;
		this.y = position.y;
	}

	/** Sets the x and y-coordinates of circle center
	 * @param x The x-coordinate
	 * @param y The y-coordinate */
	public void setPosition (float x, float y) {
		this.x = x;
		this.y = y;
	}

	/** Sets the x-coordinate of circle center
	 * @param x The x-coordinate */
	public void setX (float x) {
		this.x = x;
	}

	/** Sets the y-coordinate of circle center
	 * @param y The y-coordinate */
	public void setY (float y) {
		this.y = y;
	}

	/** Sets the radius of circle
	 * @param radius The radius */
	public void setRadius (float radius) {
		this.radius = radius;
	}

	/** Checks whether or not this circle contains a given point.
	 * 
	 * @param x X coordinate
	 * @param y Y coordinate
	 * 
	 * @return true if this circle contains the given point. */
	public boolean contains (float x, float y) {
		x = this.x - x;
		y = this.y - y;
		return x * x + y * y <= radius * radius;
	}

	/** Checks whether or not this circle contains a given point.
	 * 
	 * @param point The {@link Vector2f} that contains the point coordinates.
	 * 
	 * @return true if this circle contains this point; false otherwise. */
	public boolean contains(Vector2f point) {
		float dx = x - point.x;
		float dy = y - point.y;
		return dx * dx + dy * dy <= radius * radius;
	}

	/** @param c the other {@link Circle}
	 * @return whether this circle contains the other circle. */
	public boolean contains (Circle c) {
		final float radiusDiff = radius - c.radius;
		if (radiusDiff < 0f) return false; // Can't contain bigger circle
		final float dx = x - c.x;
		final float dy = y - c.y;
		final float dst = dx * dx + dy * dy;
		final float radiusSum = radius + c.radius;
		return (!(radiusDiff * radiusDiff < dst) && (dst < radiusSum * radiusSum));
	}

	/** @param c the other {@link Circle}
	 * @return whether this circle overlaps the other circle. */
	public boolean overlaps (Circle c) {
		float dx = x - c.x;
		float dy = y - c.y;
		float distance = dx * dx + dy * dy;
		float radiusSum = radius + c.radius;
		return distance < radiusSum * radiusSum;
	}

	/** Returns a {@link String} representation of this {@link Circle} of the form {@code x,y,radius}. */
	@Override
	public String toString () {
		return x + "," + y + "," + radius;
	}

	/** @return The circumference of this circle (as 2 * {@link FastMath#TWO_PI}) * {@code radius} */
	public float circumference () {
		return this.radius * FastMath.TWO_PI;
	}

	/** @return The area of this circle (as {@link FastMath#PI} * radius * radius). */
	public float area () {
		return this.radius * this.radius * FastMath.PI;
	}

	@Override
	public boolean equals (Object o) {
		if (o == this) return true;
		if (o == null || o.getClass() != this.getClass()) return false;
		Circle c = (Circle)o;
		return this.x == c.x && this.y == c.y && this.radius == c.radius;
	}

	@Override
	public int hashCode () {
		final int prime = 41;
		int result = 1;
		result = prime * result + Float.floatToRawIntBits(radius);
		result = prime * result + Float.floatToRawIntBits(x);
		result = prime * result + Float.floatToRawIntBits(y);
		return result;
	}
}
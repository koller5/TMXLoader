package com.jme3.tmx.util;

import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.texture.Image;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.tmx.math2d.Ellipse;
import com.jme3.tmx.math2d.Polygon;
import com.jme3.tmx.math2d.Polyline;
import com.jme3.tmx.math2d.Rectangle;
import com.jme3.util.BufferUtils;

public class ObjectTexture {

	public static enum BlendMode {

		/**
		 * Ignore the original color and just set the color of the pixel to the
		 * incoming value..
		 */
		SET {
			@Override
			protected void apply(ColorRGBA color, float r, float g, float b,
					float a) {
				color.r = r;
				color.g = g;
				color.b = b;
				color.a = a;
			}

			@Override
			protected boolean needsOriginal(float a) {
				return false;
			}
		},
		/**
		 * Add the original color and the new color based on the transparency of
		 * the new color. This will lighten the image. Original Alpha is
		 * increased by incoming alpha.
		 */
		/**
		 * Mix the original color with the new color based on the transparency
		 * of the new color Original Alpha is merged with the incoming alpha.
		 */
		NORMAL {
			@Override
			protected void apply(ColorRGBA color, float r, float g, float b,
					float a) {
				color.r = color.r * (1 - a) + r * a;
				color.g = color.g * (1 - a) + g * a;
				color.b = color.b * (1 - a) + b * a;
				color.a = color.a * (1 - a) + a;
			}

			@Override
			protected boolean needsOriginal(float a) {
				return a < 1;
			}
		},
		/**
		 * Add the original color and the new color based on the transparency of
		 * the new color. This will lighten the image. Original Alpha is
		 * increased by incoming alpha.
		 */
		ADD {
			@Override
			protected void apply(ColorRGBA color, float r, float g, float b,
					float a) {
				color.r = color.r + r * a;
				color.g = color.g + g * a;
				color.b = color.b + b * a;
				color.a = color.a + a;
			}

			@Override
			protected boolean needsOriginal(float a) {
				return true;
			}
		},
		/**
		 * Subtract the new color from the original color based on the
		 * transparency of the new color. This will darken the image. Original
		 * Alpha is increased by incoming alpha.
		 */
		SUBTRACT {
			@Override
			protected void apply(ColorRGBA color, float r, float g, float b,
					float a) {
				color.r = color.r - r * a;
				color.g = color.g - g * a;
				color.b = color.b - b * a;
				color.a = color.a + a;
			}

			@Override
			protected boolean needsOriginal(float a) {
				return true;
			}
		},
		/**
		 * Mix the original color with the new color based on the transparency
		 * of the new color If the resulting channel (r,g,b are processed
		 * separately) would be darker than the original then keep the original.
		 * Original Alpha is left unchanged.
		 */
		LIGHTEN_ONLY {
			@Override
			protected void apply(ColorRGBA color, float r, float g, float b,
					float a) {
				color.r = Math.max(color.r * (1 - a) + r * a, color.r);
				color.g = Math.max(color.g * (1 - a) + g * a, color.g);
				color.b = Math.max(color.b * (1 - a) + b * a, color.b);
			}

			@Override
			protected boolean needsOriginal(float a) {
				return true;
			}
		},
		/**
		 * Mix the original color with the new color based on the transparency
		 * of the new color If the resulting channel (r,g,b are processed
		 * separately) would be lighter than the original then keep the
		 * original. Original Alpha is left unchanged.
		 */
		DARKEN_ONLY {
			@Override
			protected void apply(ColorRGBA color, float r, float g, float b,
					float a) {
				color.r = Math.min(color.r * (1 - a) + r * a, color.r);
				color.g = Math.min(color.g * (1 - a) + g * a, color.g);
				color.b = Math.min(color.b * (1 - a) + b * a, color.b);
			}

			@Override
			protected boolean needsOriginal(float a) {
				return true;
			}
		},
		/**
		 * Mix the original color with the new color by multiplying them
		 * together (r,g,b are processed separately). This will tend to darken
		 * the image. Original Alpha is left unchanged.
		 */
		MULTIPLY {
			@Override
			protected void apply(ColorRGBA color, float r, float g, float b,
					float a) {
				color.r = color.r * (1 - a) + (r * color.r) * a;
				color.g = color.g * (1 - a) + (g * color.g) * a;
				color.b = color.b * (1 - a) + (b * color.b) * a;
			}

			@Override
			protected boolean needsOriginal(float a) {
				return true;
			}
		},
		/**
		 * Mix the original color with the new color by multiplying the inverses
		 * together (r,g,b are processed separately). This will tend to lighten
		 * the image. Original Alpha is left unchanged.
		 */
		SCREEN {
			@Override
			protected void apply(ColorRGBA color, float r, float g, float b,
					float a) {
				color.r = color.r * (1 - a) + (1 - (1 - r) * (1 - color.r)) * a;
				color.g = color.g * (1 - a) + (1 - (1 - g) * (1 - color.g)) * a;
				color.b = color.b * (1 - a) + (1 - (1 - b) * (1 - color.b)) * a;
			}

			@Override
			protected boolean needsOriginal(float a) {
				return true;
			}
		};

		/**
		 * Used to apply this blend mode to a pixel. Note that out of range
		 * values are accepted at this point. Anything greater than 1 or less
		 * than 0 will be clipped when the texture is generated.
		 */
		protected abstract void apply(ColorRGBA color, float r, float g,
				float b, float a);

		protected abstract boolean needsOriginal(float a);
	}

	Image image;
	ImageRaster imageRaster;
	ColorRGBA working;
	BlendMode mode = BlendMode.SET;

	public ObjectTexture(int width, int height) {
		image = new Image(Image.Format.RGBA8, width, height,
				BufferUtils.createByteBuffer(width * 4 * height), null,
				ColorSpace.Linear);
		imageRaster = ImageRaster.create(image);

		working = new ColorRGBA();
	}

	public ObjectTexture(Ellipse ellipse) {

	}

	public ObjectTexture(Rectangle rect) {

	}

	public ObjectTexture(Polygon polygon) {

	}

	public ObjectTexture(Polyline polyline) {

	}

	/**
	 * Paints the specified area within the Image with the specified color. The
	 * rectangle will be clipped to the bounds of the Image.
	 * 
	 * @param startX
	 *            The X coordinate of the bottom left corner of the rectangle
	 * @param startY
	 *            The Y coordinate of the bottom left corner of the rectangle
	 * @param width
	 *            The width of the rectangle
	 * @param height
	 *            The height of the rectangle
	 * @param color
	 *            The color to paint the area with.
	 */
	public final void paintRect(int startX, int startY, int width, int height, ColorRGBA color) {
		int endX = startX + width;
		int endY = startY + height;
		if (startX < 0) {
			startX = 0;
		}
		if (startY < 0) {
			startY = 0;
		}
		if (endX > imageRaster.getWidth()) {
			endX = imageRaster.getWidth();
		}
		if (endY > imageRaster.getHeight()) {
			endY = imageRaster.getHeight();
		}

		for (int y = startY; y < endY; y++) {
			for (int x = startX; x < endX; x++) {
				paintPixel(x, y);
			}
		}
	}

	/**
	 * Paints a straight line between two points on the ImageRaster. This is not
	 * clipped so all points must be greater than width/2 from the edges of the
	 * ImageRaster. It copes with lines in any direction including negative x
	 * and y.
	 * 
	 * The edges of the line are anti-aliased and it copes with non-integer
	 * (even smaller than 1 although the results may not be ideal) widths.
	 * Currently there is no special handling of the end of the line and it just
	 * just cut off either vertically or horizontally so wide lines may need
	 * some work at the ends to tidy them up.
	 * 
	 * @param x1
	 *            The x co-ordinate of the start of the line
	 * @param y1
	 *            The y co-ordinate of the start of the line
	 * @param x2
	 *            The x co-ordinate of the end of the line
	 * @param y2
	 *            The y co-ordinate of the end of the line
	 * @param width
	 *            The width to paint the line at (in pixels)
	 * @param color
	 *            The color with which to draw the line
	 */
	public void paintLine(int x1, int y1, int x2, int y2, float width, ColorRGBA color) {
		int xWidth = x2 - x1;
		int yHeight = y2 - y1;
		this.working.set(color);

		// Is the line more horizontal or more vertical?
		if (FastMath.abs(xWidth) > FastMath.abs(yHeight)) {
			float yStep = (float) yHeight / xWidth;
			// Trig to work out the angle needed then rotate the width
			// accordingly
			// Using atan not atan2 as we don't actually need the sign of the
			// result
			float angle = FastMath.HALF_PI + FastMath.atan(yStep);
			float rotatedHeight = width / FastMath.sin(angle);

			int xStep = xWidth < 0 ? -1 : 1;
			float y = y1 - rotatedHeight * 0.5f;

			for (int x = x1; x != x2; x += xStep, y += yStep) {

				// Draw bottom pixel at correct opacity
				working.a = color.a * (1 - (y - (int) y));
				paintPixel(x, (int) y);

				float remainder = y + rotatedHeight;

				// Draw center full opacity pixels
				for (int h = (int) y + 1; h < remainder - 1; h++) {
					paintPixel(x, h);
				}

				// Narrow lines (width < 1) may draw on same pixel twice which
				// makes the
				// line look a little dotty but we ned to handle that case
				// somehow...
				working.a = color.a * (remainder - (int) remainder);
				paintPixel(x, (int) (remainder));
			}
		} else {

			float xStep = (float) xWidth / yHeight;
			// Trig to work out the angle needed then rotate the width
			// accordingly
			// Using atan not atan2 as we don't actually need the sign of the
			// result
			float rotatedWidth = width
					/ FastMath.sin(FastMath.HALF_PI + FastMath.atan(xStep));
			int yStep = yHeight < 0 ? -1 : 1;
			float x = x1 - rotatedWidth * 0.5f;

			for (int y = y1; y != y2; x += xStep, y += yStep) {

				// Draw bottom pixel at correct opacity
				working.a = color.a * (1 - (x - (int) x));
				paintPixel((int) x, y);

				float remainder = x + rotatedWidth;

				// Draw center full opacity pixels
				for (int w = (int) x + 1; w < remainder - 1; w++) {
					paintPixel(w, y);
				}

				// Narrow lines (width < 1) may draw on same pixel twice which
				// makes the
				// line look a little dotty but we ned to handle that case
				// somehow...
				working.a = color.a * (remainder - (int) remainder);
				paintPixel((int) (remainder), y);
			}

		}
	}

	/**
	 * This method paints the specified pixel using the specified blend mode.
	 * 
	 * @param x
	 *            The x coordinate to paint (0 is at the left of the image)
	 * @param y
	 *            The y coordinate to paint (0 is at the bottom of the image)
	 * @param color
	 *            The color to paint with
	 * @param mode
	 *            The blend mode to use
	 */
	public void paintPixel(int x, int y) {
		if (working.a <= 0)
			return;
		imageRaster.setPixel(x, y, working);
	}

	public int round(double v) {
		return (int) (Math.round(v) & 0xFFFFFFFF);
	}

	public void ellipsePlotPoints(int xc, int yc, int x, int y) {
		paintPixel(xc + x, yc + y);
		paintPixel(xc - x, yc + y);
		paintPixel(xc + x, yc - y);
		paintPixel(xc - x, yc - y);
	}

	public void ellipseMidPoint(int xc, int yc, int rx, int ry) {
		int x, y, p1, p2;
		int rx2 = rx * rx, ry2 = ry * ry;
		x = 0;
		y = ry;

		/* region 1 */
		ellipsePlotPoints(xc, yc, x, y);
		p1 = round(rx2 - (rx2 * ry) + (0.25 * rx2));
		while (ry2 * x <= rx2 * y) {
			if (p1 <= 0) {
				p1 += ry2 * (2 * x + 3);
				x++;
			} else {
				p1 += ry2 * (2 * x + 3) + rx2 * (2 - 2 * y);
				x++;
				y--;
			}
			ellipsePlotPoints(xc, yc, x, y);
		}

		/* region 2 */
		p2 = round(ry2 * (x + 0.5) * (x + 0.5) + rx2 * (y - 1) * (y - 1) - rx2 * ry2);
		while (y >= 0) {
			if (p2 <= 0) {
				p2 += ry2 * (2 * x + 2) + rx2 * (3 - 2 * y);
				x++;
				y--;
			} else {
				p2 += rx2 * (3 - 2 * y);
				y--;
			}
			ellipsePlotPoints(xc, yc, x, y);
		}
	}
}

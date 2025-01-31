package net.geforcemods.securitycraft.api;

import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.gui.GuiCustomizeBlock;
import net.geforcemods.securitycraft.gui.components.GuiSlider;
import net.geforcemods.securitycraft.gui.components.GuiSlider.ISlider;
import net.geforcemods.securitycraft.network.packets.PacketSUpdateSliderValue;
import net.geforcemods.securitycraft.util.ClientUtils;
import net.minecraft.nbt.NBTTagCompound;

/**
 * A class that allows blocks that have
 * {@link CustomizableSCTE}s to have custom, "per-block"
 * options that are separate from the main SecurityCraft
 * configuration options.
 *
 * @author Geforce
 *
 * @param <T> The Class of the type of value this option should use
 */
public class Option<T> {

	private String name;

	protected T value;
	private T defaultValue;
	private T increment;
	private T minimum;
	private T maximum;

	public Option(String optionName, T value) {
		this.name = optionName;
		this.value = value;
		this.defaultValue = value;
	}

	public Option(String optionName, T value, T min, T max, T increment) {
		this.name = optionName;
		this.value = value;
		this.defaultValue = value;
		this.increment = increment;
		this.minimum = min;
		this.maximum = max;
	}

	/**
	 * Called when this option's button in {@link GuiCustomizeBlock} is pressed.
	 * Update the option's value here. <p>
	 *
	 * NOTE: This gets called on the server side, not on the client!
	 * Use TileEntitySCTE.sync() to update values on the client-side.
	 */
	public void toggle() {}

	public void copy(Option<?> option) {
		value = (T) option.getValue();
	}

	/**
	 * @return This option, casted to a boolean.
	 */
	public boolean asBoolean() {
		return (Boolean) value;
	}

	/**
	 * @return This option, casted to a integer.
	 */
	public int asInteger() {
		return (Integer) value;
	}

	/**
	 * @return This option, casted to a double.
	 */
	public double asDouble() {
		return (Double) value;
	}

	/**
	 * @return This option, casted to a float.
	 */
	public float asFloat() {
		return (Float) value;
	}

	public void readFromNBT(NBTTagCompound tag) {
		if(value instanceof Boolean)
			value = (T) ((Boolean) tag.getBoolean(name));
		else if(value instanceof Integer)
			value = (T) ((Integer) tag.getInteger(name));
		else if(value instanceof Double)
			value = (T) ((Double) tag.getDouble(name));
		else if(value instanceof Float)
			value = (T) ((Float) tag.getFloat(name));
	}

	public void writeToNBT(NBTTagCompound tag) {
		if(value instanceof Boolean)
			tag.setBoolean(name, asBoolean());
		else if(value instanceof Integer)
			tag.setInteger(name, asInteger());
		else if(value instanceof Double)
			tag.setDouble(name, asDouble());
		else if(value instanceof Float)
			tag.setFloat(name, asFloat());
	}

	/**
	 * @return This option's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return This option's value.
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Set this option's new value here.
	 *
	 * @param value The new value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * @return This option's default value.
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @return If this option is some kind of number (integer, float, etc.),
	 *         return the amount the number should increase/decrease every time
	 *         the option is toggled in {@link GuiCustomizeBlock}.
	 */
	public T getIncrement() {
		return increment;
	}

	/**
	 * @return The lowest value this option can be set to.
	 */
	public T getMin() {
		return minimum;
	}

	/**
	 * @return The highest value this option can be set to.
	 */
	public T getMax() {
		return maximum;
	}

	/**
	 * A subclass of {@link Option}, already setup to handle booleans.
	 */
	public static class OptionBoolean extends Option<Boolean>{

		public OptionBoolean(String optionName, Boolean value) {
			super(optionName, value);
		}

		@Override
		public void toggle() {
			setValue(!getValue());
		}

		@Override
		public Boolean getValue() {
			return (boolean) value;
		}

		@Override
		public String toString() {
			return (value) + "";
		}
	}

	/**
	 * A subclass of {@link Option}, already setup to handle integers.
	 */
	public static class OptionInt extends Option<Integer>{

		public OptionInt(String optionName, Integer value) {
			super(optionName, value);
		}

		public OptionInt(String optionName, Integer value, Integer min, Integer max, Integer increment) {
			super(optionName, value, min, max, increment);
		}

		@Override
		public void toggle() {
			if(getValue() >= getMax()) {
				setValue(getMin());
				return;
			}

			if((getValue() + getIncrement()) >= getMax()) {
				setValue(getMax());
				return;
			}

			setValue(getValue() + getIncrement());
		}

		@Override
		public Integer getValue() {
			return (int) value;
		}

		@Override
		public String toString() {
			return (value) + "";
		}
	}

	/**
	 * A subclass of {@link Option}, already setup to handle doubles.
	 */
	public static class OptionDouble extends Option<Double> implements ISlider{
		private boolean slider;
		private CustomizableSCTE tileEntity;

		public OptionDouble(String optionName, Double value) {
			super(optionName, value);
			slider = false;
		}

		public OptionDouble(String optionName, Double value, Double min, Double max, Double increment) {
			super(optionName, value, min, max, increment);
			slider = false;
		}

		public OptionDouble(CustomizableSCTE te, String optionName, Double value, Double min, Double max, Double increment, boolean s) {
			super(optionName, value, min, max, increment);
			slider = s;
			tileEntity = te;
		}

		@Override
		public void toggle() {
			if(isSlider())
				return;

			if(getValue() >= getMax()) {
				setValue(getMin());
				return;
			}

			if((getValue() + getIncrement()) >= getMax()) {
				setValue(getMax());
				return;
			}

			setValue(getValue() + getIncrement());
		}

		@Override
		public Double getValue() {
			return (double) value;
		}

		@Override
		public String toString() {
			return Double.toString(value).length() > 5 ? Double.toString(value).substring(0, 5) : Double.toString(value);
		}

		public boolean isSlider()
		{
			return slider;
		}

		@Override
		public void onChangeSliderValue(GuiSlider slider, String blockName, int id)
		{
			if(!isSlider())
				return;

			setValue(slider.getValue());
			slider.displayString = (ClientUtils.localize("option." + blockName + "." + getName()) + " ").replace("#", toString());
		}

		@Override
		public void onMouseRelease(int id)
		{
			SecurityCraft.network.sendToServer(new PacketSUpdateSliderValue(tileEntity.getPos(), id, getValue()));
		}
	}

	/**
	 * A subclass of {@link Option}, already setup to handle floats.
	 */
	public static class OptionFloat extends Option<Float>{

		public OptionFloat(String optionName, Float value) {
			super(optionName, value);
		}

		public OptionFloat(String optionName, Float value, Float min, Float max, Float increment) {
			super(optionName, value, min, max, increment);
		}

		@Override
		public void toggle() {
			if(getValue() >= getMax()) {
				setValue(getMin());
				return;
			}

			if((getValue() + getIncrement()) >= getMax()) {
				setValue(getMax());
				return;
			}

			setValue(getValue() + getIncrement());
		}

		@Override
		public Float getValue() {
			return value;
		}

		@Override
		public String toString() {
			return Float.toString(value).length() > 5 ? Float.toString(value).substring(0, 5) : Float.toString(value);
		}

	}

}

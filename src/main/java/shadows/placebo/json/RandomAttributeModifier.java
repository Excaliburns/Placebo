package shadows.placebo.json;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.placebo.util.RandomRange;

public class RandomAttributeModifier {

	protected final Attribute attribute;
	protected final Operation op;
	protected final RandomRange value;
	protected final UUID id;

	/**
	 * Creates a Chanced Effect Instance.
	 * @param chance The chance this potion is received.
	 * @param effect The effect.
	 * @param amp A random range of possible amplifiers.
	 */
	public RandomAttributeModifier(Attribute attribute, Operation op, RandomRange value) {
		this.attribute = attribute;
		this.op = op;
		this.value = value;
		Random rand = new Random();
		rand.setSeed(Objects.hash(attribute, op, value));
		this.id = new UUID(rand.nextLong(), rand.nextLong());
	}

	public void apply(Random rand, LivingEntity entity) {
		if (entity == null) throw new RuntimeException("Attempted to apply a random attribute modifier to a null entity!");
		AttributeModifier modif = genModifier(rand);
		AttributeInstance inst = entity.getAttribute(this.attribute);
		if (inst == null) throw new RuntimeException(String.format("Attempted to apply a random attribute modifier to an entity (%s) that does not have that attribute (%s)!", entity.getType().getRegistryName(), this.attribute.getRegistryName()));
		inst.addPermanentModifier(modif);
	}

	public AttributeModifier genModifier(Random rand) {
		return new AttributeModifier(this.id, "placebo_random_modifier_" + this.attribute.getDescriptionId(), this.value.getDouble(rand), this.op);
	}

	public Attribute getAttribute() {
		return this.attribute;
	}

	public Operation getOp() {
		return this.op;
	}

	public RandomRange getValue() {
		return this.value;
	}

	public static class Deserializer implements JsonDeserializer<RandomAttributeModifier> {

		@Override
		public RandomAttributeModifier deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) throws JsonParseException {
			JsonObject obj = json.getAsJsonObject();
			String _attribute = obj.get("attribute").getAsString();
			Operation op = ctx.deserialize(obj.get("operation"), Operation.class);
			RandomRange value;
			if (obj.get("value").isJsonObject()) {
				JsonObject valueObj = GsonHelper.getAsJsonObject(obj, "value");
				value = new RandomRange(GsonHelper.getAsDouble(valueObj, "min"), GsonHelper.getAsDouble(valueObj, "max"));
			} else {
				double v = GsonHelper.getAsDouble(obj, "value");
				value = new RandomRange(v, v);
			}
			Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(_attribute));
			if (attribute == null || value == null || op == null) throw new JsonParseException("Attempted to deserialize invalid RandomAttributeModifier: " + json.toString());
			return new RandomAttributeModifier(attribute, op, value);
		}
	}
}
package io.pmmp.pocketstorm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Value;

import org.jetbrains.annotations.NotNull;

public class WildcardString{
	@NotNull @Getter private final Component[] components;
	@Getter(lazy = true) private final String string = computeToString();
	@Getter(lazy = true) private final Pattern regex = computeRegex();

	public WildcardString(@NotNull Component[] components){
		this.components = components.clone();
	}

	public static WildcardString parseWildcard(String string){
		List<Component> components = new ArrayList<>();
		while(!string.isEmpty()){
			int asterisk = string.indexOf('*'),
					question = string.indexOf('?');
			int cut;
			if(asterisk != -1 && question != -1){
				cut = Math.min(asterisk, question);
				components.add(new Literal(string.substring(0, cut), StringLiteralUtil.LiteralType.NOWDOC));
				components.add(asterisk == cut ? new AnyChar() : new OneChar());
			}else if(asterisk != -1){
				components.add(new Literal(string.substring(0, cut = asterisk), StringLiteralUtil.LiteralType.NOWDOC));
				components.add(new AnyChar());
			}else if(question != -1){
				components.add(new Literal(string.substring(0, cut = question), StringLiteralUtil.LiteralType.NOWDOC));
				components.add(new OneChar());
			}else{
				// no more delimiters, throw everything into the last component
				components.add(new Literal(string, StringLiteralUtil.LiteralType.NOWDOC));
				continue;
			}
			string = string.substring(cut + 1);
		}
		return new WildcardString(components.toArray(new Component[components.size()]));
	}

	public interface Component{
		@Override
		@NotNull
		String toString();

		@NotNull
		String toRegex();
	}

	@Value
	public static class Literal implements Component{
		String value;
		String parsed;

		public Literal(String value, StringLiteralUtil.LiteralType type) throws IllegalArgumentException{
			this.value = value;
			parsed = StringLiteralUtil.parseStringLiteral(value, type);
		}

		@NotNull
		@Override
		public String toString(){
			return value;
		}

		@NotNull
		@Override
		public String toRegex(){
			return Pattern.quote(value);
		}
	}

	@Value
	public static class OneChar implements Component{
		@NotNull
		@Override
		public String toString(){
			return "?";
		}

		@NotNull
		@Override
		public String toRegex(){
			return ".";
		}
	}

	@Value
	public static class AnyChar implements Component{
		@NotNull
		@Override
		public String toString(){
			return "*";
		}

		@NotNull
		@Override
		public String toRegex(){
			return ".*";
		}
	}

	private String computeToString(){
		StringBuilder builder = new StringBuilder();

		//noinspection ConstantConditions
		for(Component component : components){
			builder.append(component.toString());
		}
		return builder.toString();
	}

	@Override
	public String toString(){
		return getString();
	}

	private Pattern computeRegex(){
		StringBuilder regex = new StringBuilder();
		//noinspection ConstantConditions
		for(Component component : components){
			regex.append(component.toRegex());
		}
		return Pattern.compile(regex.toString());
	}

	public boolean matches(String string){
		return getRegex().matcher(string).matches();
	}

	public boolean matches(WildcardString string){
		// TODO implement
		return matches(string.getString());
	}

}

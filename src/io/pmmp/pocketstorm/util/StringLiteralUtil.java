package io.pmmp.pocketstorm.util;

import org.jetbrains.annotations.Nullable;

import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;

public final class StringLiteralUtil{
	public static String parseStringLiteral(String literal, LiteralType type){
		StringBuilder builder = new StringBuilder(literal.length());
		char[] chars = literal.toCharArray();
		for(Dirty2<String, Integer> dirty = new Dirty2<>(null, 0); dirty.second < chars.length; ++dirty.second){
			char c = chars[dirty.second];
			if(c == '\\'){
				c = chars[++dirty.second];
				dirty.first = null;
				type.resolveAbstract(c, chars, dirty);
				builder.append(dirty.first != null ? dirty.first : ("\\" + c));
			}else{
				builder.append(c);
			}
		}
		return builder.toString();
	}

	public enum LiteralType{
		SINGLE{
			@Nullable
			@Override
			public String resolveAbstract(char escape){
				if(escape == '\\'){
					return "\\";
				}
				if(escape == '\''){
					return "'";
				}
				return null;
			}
		},
		DOUBLE{
			@Override
			public void resolveAbstract(char escape, char[] chars, Dirty2<String, Integer> dirty){
				LiteralType.normalEscapeImpl(escape, chars, dirty);
			}
		},
		HEREDOC{
			@Nullable
			@Override
			public String resolveAbstract(char escape){
				return null;
			}
		},
		NOWDOC{
			@Nullable
			@Override
			public String resolveAbstract(char escape){
				return null;
			}
		};

		public void resolveAbstract(char escape, char[] chars, Dirty2<String, Integer> dirty){
			dirty.first = resolveAbstract(escape);
		}

		@Nullable
		public String resolveAbstract(char escape){
			throw new UnsupportedOperationException("resolveAbstract() is not implemented");
		}

		public static LiteralType fromPsi(StringLiteralExpression element){
			return element.isSingleQuote() ? SINGLE :
					!element.isHeredoc() ? DOUBLE :
							element.getText().charAt(3) == '\'' ? NOWDOC : HEREDOC;
		}

		private static void normalEscapeImpl(char escape, char[] chars, Dirty2<String, Integer> dirty){
			switch(escape){
				case 'n':
					dirty.first = "\n";
					return;
				case 'r':
					dirty.first = "\r";
					return;
				case 't':
					dirty.first = "\t";
					return;
				case 'v':
					dirty.first = "\u000B";
					return;
				case 'e':
					dirty.first = "\u001B";
					return;
				case 'f':
					dirty.first = "\u000C";
					return;
				case '\\':
					dirty.first = "\\";
					return;
				case '$':
					dirty.first = "$";
					return;
				case '"':
					dirty.first = "\"";
					return;
			}
			if('0' <= escape && escape <= '7'){
				int b = escape - '0';
				for(int i = 0; i < 2; ++i){
					char c = chars[dirty.second + 1];
					if('0' <= c && c <= '7'){
						b <<= 3;
						b |= c - '0';
						++dirty.second;
					}else{
						break;
					}
				}
				dirty.first = new String(new char[]{(char) b});
				return;
			}
			if(escape == 'x'){
				int b = chars[++dirty.second] - '0';
				int next = parseHex(chars[dirty.second + 1]);
				if(next != -1){
					b <<= 4;
					b |= next;
					++dirty.second;
				}
				dirty.first = new String(new char[]{(char) b});
				return;
			}
			dirty.first = null;
		}

		private static int parseHex(char c){
			if('0' <= c && c <= '9'){
				return c - '0';
			}
			if('a' <= c && c <= 'f'){
				return 0x0A + (c - 'a');
			}
			if('A' <= c && c <= 'F'){
				return 0x0A + (c - 'A');
			}
			return -1;
		}
	}
}

package util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.Reader;
import java.util.Arrays;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Json {
	private final JSONObject inner;

	@SneakyThrows
	public static Json parse(final String str){
		val parser=new JSONParser();
		val inner=(JSONObject) parser.parse(str);
		return new Json(inner);
	}

	@SneakyThrows
	public static Json parse(final Reader reader){
		val parser=new JSONParser();
		val inner=(JSONObject) parser.parse(reader);
		return new Json(inner);
	}

	public Json getObject(final String key){
		return new Json((JSONObject) inner.get(key));
	}

	public String getString(final String key){
		return (String) inner.get(key);
	}

	public int getInt(final String key){
		val result=inner.get(key);
		if(result instanceof Integer integer)return integer;
		return Integer.parseInt((String) result);
	}

	public double getDouble(final String key){
		val result=inner.get(key);
		if(result instanceof Double doub)return doub;
		return Double.parseDouble((String) result);
	}

	public Json[] getObjectArray(final String key){
		return Arrays.stream(((Object[])inner.get(key))).map(o->new Json(((JSONObject) o))).toArray(Json[]::new);
	}
}

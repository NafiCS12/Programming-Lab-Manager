package miscelleneous;

import javafx.scene.effect.Bloom;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;

public class Effects
{
	public static DropShadow DropShadow(Effect input)
	{
		DropShadow out=new DropShadow();
		out.setRadius(4.0);
		out.setOffsetX(0.7);
		out.setOffsetY(0.7);
		out.setSpread(0.0);
		if(input!=null) out.setInput(input);
		
		return out;
	}
	
	public static BoxBlur Blur(Effect input)
	{
		BoxBlur out=new BoxBlur();
		out.setHeight(5.0);
		out.setWidth(5.0);
		out.setIterations(5);
		
		if(input!=null) out.setInput(input);
		
		return out;
	}
	
	public static Lighting Light()
	{
		Light.Distant light=new Light.Distant();
		light.setAzimuth(45.0);
		light.setElevation(30.0);
		
		Lighting out=new Lighting();
		out.setLight(light);
		out.setSurfaceScale(2);
		
		return out;
	}
	
	public static Bloom Bloom(Effect input)
	{
		Bloom out=new Bloom();
		out.setThreshold(0.3);
		
		if(input!=null) out.setInput(input);
		
		return out;
	}
	
	public static Glow Glow(Effect input)
	{
		Glow out=new Glow();
		out.setLevel(0.8);
		
		if(input!=null) out.setInput(input);
		
		return out;
	}
}

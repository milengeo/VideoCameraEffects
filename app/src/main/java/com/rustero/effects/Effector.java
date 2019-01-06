package com.rustero.effects;


import com.rustero.App;
import com.rustero.tools.Size2D;

import java.util.ArrayList;
import java.util.List;

public class Effector {

	private static Effector self;

	private List<glEffect> mList;
	private int mIndex;


	public static Effector get() {
		if (null == self)
			self = new Effector();
		return self;
	}


	private Effector() {
		mList = new ArrayList<>();
	}


	public void detach() {
		while (mList.size() > 0) {
			glEffect effect = mList.get(0);
			effect.release();
			mList.remove(0);
		}
	}


	public int getCount() {
		return mList.size();
	}


//keep
//	public List<String> getItems() {
//		List<String> result = new ArrayList<>();
//		for (glEffect effect : mList) {
//			result.add(effect.name);
//		}
//		return result;
//	}



	public glEffect getEffect(String aName) {
		glEffect result = null;
		for (glEffect effect : mList) {
			if (effect.name.equals(aName)) {
				result = effect;
				break;
			}
		}
		return result;
	}



	public void updateRotation(int aRotation) {
		for (glEffect effect : mList) {
			effect.setRotation(aRotation);
		}
	}



	public void updateEffects(List<String> aNames, Size2D aSize) {
		//first, remove the gone
		List<String> gone = new ArrayList<>();
		for (glEffect effect : mList) {
			if (!aNames.contains(effect.name))
				gone.add(effect.name);
		}
		for (String togo : gone) {
			deleteEffect(togo);
		}

		//second, add the new ones
		for (String name : aNames) {
			glEffect effect = getEffect(name);
			if (effect == null)
				createEffect(name, aSize);
		}
	}




	public boolean createEffect(String aName, Size2D aSize) {
		glEffect effect = glEffect.createEffect(aName);
		if (null == effect) return false;
		if (effect.name.isEmpty()) return false;
		effect.compile();
		effect.resize(aSize);
		mList.add(effect);
		return true;
	}


	public boolean deleteEffect(String aName) {
		glEffect effect = null;
		for (glEffect ef : mList) {
			if (ef.name.equals(aName)) {
				effect = ef;
				break;
			}
		}
		if (null == effect) return false;
		effect.release();
		mList.remove(effect);
		return true;
	}



	public void  goFirst() {
		mIndex = 0;
	}


	public void goNext() {
		mIndex++;
	}


	public glEffect getCurrent() {
		if (mIndex < mList.size())
			return mList.get(mIndex);
		else
			return null;
	}

}

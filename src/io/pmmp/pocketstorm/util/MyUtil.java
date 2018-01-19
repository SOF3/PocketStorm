package io.pmmp.pocketstorm.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.Lombok;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jetbrains.php.PhpIndex;
import com.jetbrains.php.lang.psi.elements.*;

public final class MyUtil{
	@NotNull
	public static VirtualFile lazyCreateChildDir(Object requester, VirtualFile file, String path) throws IOException{
		return lazyCreateChildDir(requester, file, path.split("[\\\\/]+"));
	}

	@NotNull
	public static VirtualFile lazyCreateChildDir(Object requester, VirtualFile file, String[] path) throws IOException{
		for(String name : path){
			VirtualFile child;
			if((child = file.findChild(name)) != null){
				file = child;
			}else{
				file = file.createChildDirectory(requester, name);
			}
		}
		return file;
	}

	@SuppressWarnings("ConstantConditions")
	public static RuntimeException s(@NotNull Throwable delegate){
		throw Lombok.sneakyThrow(delegate);
	}

	public static boolean classExtends(@NotNull PhpClass clazz, @NotNull String fqn){
		if(fqn.charAt(0) != '\\'){
			fqn = "\\" + fqn;
		}

		return clazz.getFQN().equals(fqn) || clazz.getSuperClass() != null && classExtends(clazz.getSuperClass(), fqn);
	}

	public static boolean classImplements(@NotNull PhpClass clazz, @NotNull String fqn){
		if(fqn.charAt(0) != '\\'){
			fqn = "\\" + fqn;
		}
		for(PhpClass itf : clazz.getImplementedInterfaces()){
			if(itf.getFQN().equals(fqn) || classImplements(itf, fqn)){
				return true;
			}
		}
		return false;
	}

	public static boolean doesMethodImplement(MethodReference ref, String requiredInterface, String requiredMethod){
		PhpIndex index = PhpIndex.getInstance(ref.getProject());
		PhpExpression typeRef = ref.getClassReference();
		if(!requiredMethod.equalsIgnoreCase(ref.getName())){
			return false;
		}
		if(!(typeRef instanceof PhpReference)){
			return false;
		}
		String signature = ((PhpReference) typeRef).getSignature();
		Collection<? extends PhpNamedElement> typeElements = index.getBySignature(signature);
		for(PhpNamedElement typeElement : typeElements){
			if(typeElement instanceof PhpClass){
				if(classImplements((PhpClass) typeElement, requiredInterface)){
					return true;
				}
			}
		}
		return false;
	}

	public static boolean doesMethodExtend(MethodReference ref, String requiredSuperclass, String requiredMethod){
		PhpIndex index = PhpIndex.getInstance(ref.getProject());
		PhpExpression typeRef = ref.getClassReference();
		if(!requiredMethod.equalsIgnoreCase(ref.getName())){
			return false;
		}
		if(!(typeRef instanceof PhpReference)){
			return false;
		}
		String signature = ((PhpReference) typeRef).getSignature();
		Collection<? extends PhpNamedElement> typeElements = index.getBySignature(signature);
		for(PhpNamedElement typeElement : typeElements){
			if(typeElement instanceof PhpClass){
				if(classExtends((PhpClass) typeElement, requiredSuperclass)){
					return true;
				}
			}
		}
		return false;
	}

	@Nullable
	public static WildcardString resolveStringLiteral(PsiElement element){
		List<WildcardString.Component> components = resolveStringLiteral(new ArrayList<>(), element);
		return components != null ? new WildcardString(components.toArray(new WildcardString.Component[0])) : null;
	}

	@Nullable
	public static List<WildcardString.Component> resolveStringLiteral(List<WildcardString.Component> list, PsiElement element){
		if(element instanceof StringLiteralExpression){
			PsiElement[] children = element.getChildren();
			for(int i = 1; i < children.length - 1; i++){
				if(children[i] instanceof Variable){
					list.add(new WildcardString.AnyChar());
				}else if(children[i] instanceof LeafPsiElement){
					try{
						list.add(new WildcardString.Literal(children[i].getText(), StringLiteralUtil.LiteralType.fromPsi((StringLiteralExpression) element)));
					}catch(IllegalArgumentException e){
						return null;
					}
				}
			}
		}
		return list;
	}

	public static boolean isIn(VirtualFile parent, VirtualFile child){
		do{
			if(parent.equals(child)){
				return true;
			}
		}while((child = child.getParent()) != null);
		return false;
	}
}

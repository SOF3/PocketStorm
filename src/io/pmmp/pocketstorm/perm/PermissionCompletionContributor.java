package io.pmmp.pocketstorm.perm;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.php.lang.psi.PhpPsiUtil;
import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.ParameterList;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import io.pmmp.pocketstorm.util.MyUtil;

public class PermissionCompletionContributor extends CompletionContributor{
	public PermissionCompletionContributor(){
		extend(CompletionType.BASIC, PlatformPatterns.psiElement().withParent(StringLiteralExpression.class), new CompletionProvider<CompletionParameters>(){
			@Override
			protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result){
				PsiElement pos, parent, grandParent, greatParent;

				pos = parameters.getOriginalPosition();
				parent = PhpPsiUtil.getParentByCondition(pos, true, StringLiteralExpression.INSTANCEOF);
				if(parent instanceof StringLiteralExpression &&
						(grandParent = parent.getParent()) instanceof ParameterList &&
						(greatParent = grandParent.getParent()) instanceof MethodReference &&
						MyUtil.doesMethodImplement((MethodReference) greatParent, "pocketmine\\permission\\Permissible", "hasPermission")){
					RegisteredPermissionCache.find(parameters.getOriginalFile().getProject()).forEachOrdered(cache ->
							cache.getValue().forEach(perm -> result.addElement(LookupElementBuilder.create(perm))));
				}
			}
		});
	}
}

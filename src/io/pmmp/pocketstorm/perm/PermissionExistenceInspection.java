package io.pmmp.pocketstorm.perm;

import java.awt.BorderLayout;
import java.util.Arrays;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ui.ListTable;
import com.intellij.codeInspection.ui.ListWrappingTableModel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.containers.OrderedSet;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.jetbrains.php.lang.psi.elements.MethodReference;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import com.jetbrains.php.lang.psi.visitors.PhpElementVisitor;
import com.siyeh.ig.ui.UiUtils;
import io.pmmp.pocketstorm.inspections.PocketInspection;
import io.pmmp.pocketstorm.util.MyUtil;
import io.pmmp.pocketstorm.util.StringLiteralUtil;
import io.pmmp.pocketstorm.util.WildcardString;

public class PermissionExistenceInspection extends PocketInspection{
	public OrderedSet<String> declaredPermissions = new OrderedSet<>(Arrays.asList("pocketmine.?", "pocketmine.*"));

	@NotNull
	@Override
	public String getShortName(){
		return "PocketPermissionExistenceInspection";
	}

	@Nls
	@NotNull
	@Override
	public String getDisplayName(){
		return "Permission is not declared";
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder problemsHolder, boolean onTheFly){
		return new PhpElementVisitor(){
			@Override
			public void visitPhpMethodReference(MethodReference method){
				if(!MyUtil.doesMethodImplement(method, "pocketmine\\permission\\Permissible", "hasPermission")){
					return;
				}
				PsiElement[] parameters = method.getParameters();
				if(parameters.length < 1){
					return;
				}
//				WildcardString perm = MyUtil.resolveStringLiteral(parameters[0]);
//				if(perm == null){
//					return;
//				}
				if(!(parameters[0] instanceof StringLiteralExpression)){
					return;
				}
				StringLiteralExpression expr = (StringLiteralExpression) parameters[0];
				WildcardString perm = new WildcardString(new WildcardString.Component[]{
						new WildcardString.Literal((expr).getContents(),
								StringLiteralUtil.LiteralType.fromPsi(expr))
				});

				VirtualFile srcParent = MyUtil.findSourceParent(method);
				if(srcParent == null){
					return;
				}

				RegisteredPermissionCache cache = RegisteredPermissionCache.getInstance(srcParent);
				for(String declared : cache.getValue()){
					if(perm.matches(declared)){
						return;
					}
				}
				for(String declaredPermission : declaredPermissions){
					if(WildcardString.parseWildcard(declaredPermission).matches(perm)){
						return;
					}
				}
				problemsHolder.registerProblem(expr, String.format("The permission \"%s\" is not declared anywhere", perm.getString()), ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
			}
		};
	}

	@Nullable
	@Override
	public JComponent createOptionsPanel(){
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JBLabel("Declared permissions (wildcard allowed)"));
		ListTable lt = new ListTable(new ListWrappingTableModel(declaredPermissions, "Pattern"));
		panel.add(UiUtils.createAddRemovePanel(lt));
		return panel;
	}

	@Override
	public void resetOptions(){
		super.resetOptions();
		declaredPermissions.clear();
		declaredPermissions.addAll(Arrays.asList("pocketmine.?", "pocketmine.*"));
	}

	@NotNull
	@Override
	public HighlightDisplayLevel getDefaultLevel(){
		return HighlightDisplayLevel.WARNING;
	}
}

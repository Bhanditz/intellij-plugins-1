package org.stepik.plugin.projectView;

import com.intellij.ide.navigationToolbar.JavaNavBarExtension;
import com.intellij.psi.PsiElement;
import com.jetbrains.tmp.learning.StudyTaskManager;
import org.jetbrains.annotations.Nullable;

/**
 * @author meanmail
 */
public class IdeaNavBarModelExtension extends JavaNavBarExtension {
    @Nullable
    @Override
    public String getPresentableText(@Nullable final Object object) {
        String text = NavBarModelExtensionUtils.getPresentableText(object);
        return text != null ? text : super.getPresentableText(object);
    }

    @Nullable
    @Override
    public PsiElement adjustElement(final PsiElement psiElement) {
        PsiElement element = NavBarModelExtensionUtils.adjustElement(psiElement);
        return element == null ? null : super.adjustElement(psiElement);
    }
}
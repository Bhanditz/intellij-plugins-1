package org.stepik.plugin.projectView.pycharm;

import com.intellij.ide.navigationToolbar.DefaultNavBarExtension;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import org.jetbrains.annotations.Nullable;
import org.stepik.plugin.projectView.NavBarModelExtensionUtils;

/**
 * @author meanmail
 */
public class PyCharmNavBarModelExtension extends DefaultNavBarExtension {
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

    @Override
    public boolean processChildren(Object object, Object rootElement, Processor<Object> processor) {
        return true;
    }
}
package me.vickychijwani.kotlinkoans.features.viewkoan

import android.arch.lifecycle.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import me.vickychijwani.kotlinkoans.R
import me.vickychijwani.kotlinkoans.data.Koan
import me.vickychijwani.kotlinkoans.features.common.*
import me.vickychijwani.kotlinkoans.util.*
import org.xml.sax.InputSource
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory


class KoanDescriptionFragment(): Fragment(), Observer<KoanViewModel.KoanData> {

    companion object {
        fun newInstance(): KoanDescriptionFragment {
            val fragment = KoanDescriptionFragment()
            fragment.arguments = Bundle.EMPTY
            return fragment
        }
    }

    private var SECTION_PADDING_HORIZONTAL = 0
    private var SECTION_PADDING_VERTICAL = 0
    private var mKoan: Koan? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)

        val vm = ViewModelProviders.of(activity!!).get(KoanViewModel::class.java)
        vm.liveData.observe(activity as LifecycleOwner, this@KoanDescriptionFragment)

        SECTION_PADDING_HORIZONTAL = getOffsetDimen(context, R.dimen.padding_large)
        // vertical padding is half of horizontal because it adds up between 2 sections
        SECTION_PADDING_VERTICAL = SECTION_PADDING_HORIZONTAL / 2

        return inflater.inflate(R.layout.fragment_koan_description, container, false)
    }

    override fun onChanged(koanData: KoanViewModel.KoanData?) {
        mKoan = koanData?.koan
        showKoan()
    }

    private fun showKoan() {
        val koan = mKoan
        logDebug { "Updating view, current koan is ${koan?.name}" }
        if (koan == null) {
            return
        }

        /**
         * Split the description into sections, where each section is either:
         *
         *   1. The code inside a code block (i.e., the stuff inside <pre><code>...</code></pre>),
         *   2. OR, some HTML without ANY code blocks
         *
         * Then display sections of type (1) in a syntax-highlighted code viewer, and those of
         * type (2) using regular Html.fromHtml().
         */
        val sectionContainer = view?.findViewById(R.id.section_container) as ViewGroup
        sectionContainer.removeAllViews()
        buildSections(koan.descriptionHtml)
                .map(this::getViewForSection)
                .forEach { sectionContainer.addView(it) }
    }

    private fun buildSections(html: String): List<Section> {
        val sections = mutableListOf<Section>()
        var section = StringWriter()
        val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc = docBuilder.parse(InputSource(StringReader("<html>$html</html>")))
        loop@ for (node in doc.firstChild.childNodes) {
            when {
                node.nodeName == "#comment" -> continue@loop
                node.nodeName == "#text" -> section.append(node.nodeValue)
                node.nodeName == "pre" -> {
                    // add the previous section accumulated so far
                    if (section.toString().isNotBlank()) {
                        sections.add(Section(section.toString(), isCodeSection = false))
                    }
                    // add the code section
                    sections.add(Section(node.textContent, isCodeSection = true))
                    // start the next section
                    section = StringWriter()
                }
                // we haven't encountered a code block, so keep accumulating the current section
                else -> section.append(node.outerHTML)
            }
        }
        // add the last accumulated section, if any
        if (section.toString().isNotBlank()) {
            sections.add(Section(section.toString(), isCodeSection = false))
        }
        return sections
    }

    private fun getViewForSection(section: Section): View {
        if (section.isCodeSection) {
            val codeBlockRoot = activity!!.layoutInflater
                    .inflate(R.layout.embedded_code_block, null, false)
            val codeBlock = codeBlockRoot.findViewById(R.id.code_block) as CodeEditText
            codeBlock.setText(section.content)
            codeBlock.setupForViewing()
            val codePadding = codeBlockRoot.findViewById(R.id.code_padding) as View
            codePadding.setPadding(SECTION_PADDING_HORIZONTAL, SECTION_PADDING_VERTICAL,
                    SECTION_PADDING_HORIZONTAL, SECTION_PADDING_VERTICAL)
            return codeBlockRoot
        } else {
            return makeTextView(context!!, section.content, isHtml = true,
                    paddingStart = SECTION_PADDING_HORIZONTAL, paddingEnd = SECTION_PADDING_HORIZONTAL,
                    paddingTop = SECTION_PADDING_VERTICAL, paddingBottom = SECTION_PADDING_VERTICAL,
                    textAppearance = R.style.TextAppearance_Small)
        }
    }

    class Section(content: String, val isCodeSection: Boolean) {
        val content = content.trim()
    }

}

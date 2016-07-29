/*
 * Created on 25/ago/2011
 * Copyright 2011 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * 
 * This file is part of the Sejda source code
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.model.parameter;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sejda.common.collection.NullSafeSet;
import org.sejda.model.optimization.OptimizationPolicy;
import org.sejda.model.parameter.base.DiscardableOutlineTaskParameters;
import org.sejda.model.parameter.base.MultiplePdfSourceMultipleOutputParameters;
import org.sejda.model.parameter.base.OptimizableOutputTaskParameters;
import org.sejda.model.pdf.page.PageRange;
import org.sejda.model.pdf.page.PageRangeSelection;
import org.sejda.model.pdf.page.PagesSelection;
import org.sejda.model.pdf.page.PredefinedSetOfPages;
import org.sejda.model.validation.constraint.HasSelectedPages;
import org.sejda.model.validation.constraint.NoIntersections;
import org.sejda.model.validation.constraint.NotAllowed;

/**
 * Parameter class for an Extract pages task. Allow to specify a predefined set of pages to extract (odd, even) or a set of page ranges but not both. Page ranges are validated to
 * make sure that there is no intersection.
 * 
 * @author Andrea Vacondio
 * 
 */
@NoIntersections
@HasSelectedPages
public class ExtractPagesParameters extends MultiplePdfSourceMultipleOutputParameters implements PageRangeSelection,
        PagesSelection, OptimizableOutputTaskParameters, DiscardableOutlineTaskParameters {

    @NotNull
    private OptimizationPolicy optimizationPolicy = OptimizationPolicy.NO;
    private boolean discardOutline = false;
    @NotNull
    @NotAllowed(disallow = { PredefinedSetOfPages.ALL_PAGES })
    private PredefinedSetOfPages predefinedSetOfPages;
    @Valid
    private final Set<PageRange> pageSelection = new NullSafeSet<PageRange>();
    private boolean invertSelection = false;

    /**
     * Creates and empty instance where page selection can be set
     */
    public ExtractPagesParameters() {
        this.predefinedSetOfPages = PredefinedSetOfPages.NONE;
    }

    /**
     * Creates an instance using a predefined set of pages to extract.
     * 
     * @param predefinedSetOfPages
     */
    public ExtractPagesParameters(PredefinedSetOfPages predefinedSetOfPages) {
        this.predefinedSetOfPages = predefinedSetOfPages;
    }

    public void addPageRange(PageRange range) {
        pageSelection.add(range);
    }

    public void addAllPageRanges(Collection<PageRange> ranges) {
        pageSelection.addAll(ranges);
    }

    public PredefinedSetOfPages getPredefinedSetOfPages() {
        return predefinedSetOfPages;
    }

    /**
     * @return an unmodifiable view of the pageSelection
     */
    @Override
    public Set<PageRange> getPageSelection() {
        return Collections.unmodifiableSet(pageSelection);
    }

    /**
     * @param upperLimit
     *            the number of pages of the document (upper limit).
     * @return the selected set of pages. Iteration ordering is predictable, it is the order in which elements were inserted into the {@link PageRange} set or the natural order in
     *         case of {@link PredefinedSetOfPages}.
     * @see PagesSelection#getPages(int)
     */
    @Override
    public Set<Integer> getPages(int upperLimit) {
        Set<Integer> pages = new NullSafeSet<Integer>();
        if (predefinedSetOfPages != PredefinedSetOfPages.NONE) {
            pages = predefinedSetOfPages.getPages(upperLimit);
        } else {
            for (PageRange range : getPageSelection()) {
                pages.addAll(range.getPages(upperLimit));
            }
        }

        if(!invertSelection) {
            return pages;
        }

        Set<Integer> invertedPages = new NullSafeSet<Integer>();
        for(int i = 1; i <= upperLimit; i++) {
            if(!pages.contains(i)) {
                invertedPages.add(i);
            }
        }

        return invertedPages;
    }

    @Override
    public OptimizationPolicy getOptimizationPolicy() {
        return optimizationPolicy;
    }

    @Override
    public void setOptimizationPolicy(OptimizationPolicy optimizationPolicy) {
        this.optimizationPolicy = optimizationPolicy;
    }

    @Override
    public boolean discardOutline() {
        return discardOutline;
    }

    @Override
    public void discardOutline(boolean discardOutline) {
        this.discardOutline = discardOutline;
    }

    public boolean isInvertSelection() {
        return invertSelection;
    }

    public void setInvertSelection(boolean invertSelection) {
        this.invertSelection = invertSelection;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(optimizationPolicy).append(discardOutline)
                .append(predefinedSetOfPages).append(invertSelection).append(pageSelection).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ExtractPagesParameters)) {
            return false;
        }
        ExtractPagesParameters parameter = (ExtractPagesParameters) other;
        return new EqualsBuilder().appendSuper(super.equals(other))
                .append(predefinedSetOfPages, parameter.predefinedSetOfPages)
                .append(optimizationPolicy, parameter.optimizationPolicy)
                .append(discardOutline, parameter.discardOutline)
                .append(pageSelection, parameter.pageSelection)
                .append(invertSelection, parameter.invertSelection)
                .isEquals();
    }
}

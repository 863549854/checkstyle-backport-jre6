////////////////////////////////////////////////////////////////////////////////
// checkstyle: Checks Java source code for adherence to a set of rules.
// Copyright (C) 2001-2017 the original author or authors.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
////////////////////////////////////////////////////////////////////////////////

package com.puppycrawl.tools.checkstyle.filters;

import java.util.regex.Pattern;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.Filter;
import com.puppycrawl.tools.checkstyle.jre6.util.Objects;
import com.puppycrawl.tools.checkstyle.utils.CommonUtils;

/**
 * This filter processes {@link AuditEvent}
 * objects based on the criteria of file, check, module id, line, and
 * column. It rejects an AuditEvent if the following match:
 * <ul>
 *   <li>the event's file name; and</li>
 *   <li>the check name or the module identifier; and</li>
 *   <li>(optionally) the event's line is in the filter's line CSV; and</li>
 *   <li>(optionally) the check's columns is in the filter's column CSV.</li>
 * </ul>
 *
 * @author Rick Giles
 */
public class SuppressElement
    implements Filter {
    /** The regexp to match file names against. */
    private final Pattern fileRegexp;

    /** The pattern for file names. */
    private final String filePattern;

    /** The regexp to match check names against. */
    private final Pattern checkRegexp;

    /** The pattern for check class names. */
    private final String checkPattern;

    /** Module id filter. */
    private final String moduleId;

    /** Line number filter. */
    private final CsvFilter lineFilter;

    /** CSV for line number filter. */
    private final String linesCsv;

    /** Column number filter. */
    private final CsvFilter columnFilter;

    /** CSV for column number filter. */
    private final String columnsCsv;

    /**
     * Constructs a {@code SuppressElement} for a
     * file name pattern.
     *
     * @param files   regular expression for names of filtered files.
     * @param checks  regular expression for filtered check classes.
     * @param modId   the id
     * @param lines   lines CSV values and ranges for line number filtering.
     * @param columns columns CSV values and ranges for column number filtering.
     */
    public SuppressElement(String files, String checks,
                           String modId, String lines, String columns) {
        filePattern = files;
        fileRegexp = Pattern.compile(files);
        checkPattern = checks;
        if (checks == null) {
            checkRegexp = null;
        }
        else {
            checkRegexp = CommonUtils.createPattern(checks);
        }
        moduleId = modId;
        linesCsv = lines;
        if (lines == null) {
            lineFilter = null;
        }
        else {
            lineFilter = new CsvFilter(lines);
        }
        columnsCsv = columns;
        if (columns == null) {
            columnFilter = null;
        }
        else {
            columnFilter = new CsvFilter(columns);
        }
    }

    @Override
    public boolean accept(AuditEvent event) {
        return isFileNameAndModuleNotMatching(event)
                || isLineAndColumnMatch(event);
    }

    /**
     * Is matching by file name and Check name.
     * @param event event
     * @return true is matching
     */
    private boolean isFileNameAndModuleNotMatching(AuditEvent event) {
        return event.getFileName() == null
                || !fileRegexp.matcher(event.getFileName()).find()
                || event.getLocalizedMessage() == null
                || moduleId != null && !moduleId.equals(event.getModuleId())
                || checkRegexp != null && !checkRegexp.matcher(event.getSourceName()).find();
    }

    /**
     * Whether line and column match.
     * @param event event to process.
     * @return true if line and column match.
     */
    private boolean isLineAndColumnMatch(AuditEvent event) {
        return (lineFilter != null || columnFilter != null)
                && (lineFilter == null || !lineFilter.accept(event.getLine()))
                && (columnFilter == null || !columnFilter.accept(event.getColumn()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePattern, checkPattern, moduleId, linesCsv, columnsCsv);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        final SuppressElement suppressElement = (SuppressElement) other;
        return Objects.equals(filePattern, suppressElement.filePattern)
                && Objects.equals(checkPattern, suppressElement.checkPattern)
                && Objects.equals(moduleId, suppressElement.moduleId)
                && Objects.equals(linesCsv, suppressElement.linesCsv)
                && Objects.equals(columnsCsv, suppressElement.columnsCsv);
    }
}

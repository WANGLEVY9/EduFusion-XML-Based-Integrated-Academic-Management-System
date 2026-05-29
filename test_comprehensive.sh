#!/bin/bash
# ============================================================
# EduFusion Comprehensive System Test Script
# Tests all 6 API operations across all 3 colleges
# with boundary cases, validations, and error scenarios
# ============================================================

BASE_URL="http://localhost:8080/api/xml"
PASS=0
FAIL=0
RESULTS=()

# Helper: colored output
green() { printf "\033[32m%s\033[0m\n" "$1"; }
red() { printf "\033[31m%s\033[0m\n" "$1"; }
yellow() { printf "\033[33m%s\033[0m\n" "$1"; }

# Helper: send XML request and check if response contains expected text
check() {
    local test_name="$1"
    local xml_body="$2"
    local expected_text="$3"
    local not_expected="${4:-}"

    local response
    response=$(curl -s -X POST -H "Content-Type: application/xml" -d "$xml_body" "$BASE_URL" 2>&1)

    if echo "$response" | grep -q "$expected_text"; then
        if [ -n "$not_expected" ] && echo "$response" | grep -q "$not_expected"; then
            red "FAIL: $test_name"
            echo "  Expected to NOT contain: $not_expected"
            echo "  Response: $response"
            FAIL=$((FAIL + 1))
            RESULTS+=("FAIL: $test_name")
        else
            green "PASS: $test_name"
            PASS=$((PASS + 1))
            RESULTS+=("PASS: $test_name")
        fi
    else
        red "FAIL: $test_name"
        echo "  Expected: $expected_text"
        echo "  Response: $response"
        FAIL=$((FAIL + 1))
        RESULTS+=("FAIL: $test_name")
    fi
}

# ============================================================
echo "========================================"
echo "EduFusion Comprehensive Test Suite"
echo "Server: $BASE_URL"
echo "Date: $(date)"
echo "========================================"
echo ""

# ============================================================
# 1. HEALTH CHECK
# ============================================================
echo "--- [1/9] Health Check ---"

# Direct GET to /api/health (not /api/xml)
health_resp=$(curl -s http://localhost:8080/api/health 2>&1)
if [ "$health_resp" = "OK" ]; then
    green "PASS: Health endpoint (GET /api/health)"
    PASS=$((PASS + 1))
    RESULTS+=("PASS: Health endpoint (GET /api/health)")
else
    red "FAIL: Health endpoint - got: $health_resp"
    FAIL=$((FAIL + 1))
    RESULTS+=("FAIL: Health endpoint")
fi

# ============================================================
# 2. QUERY COURSES (queryCourses) - All 3 colleges
# ============================================================
echo ""
echo "--- [2/9] Query Courses ---"

# College A
check "queryCourses - College A" \
    '<request><type>queryCourses</type><college>A</college></request>' \
    'A101' ''

# College B
check "queryCourses - College B" \
    '<request><type>queryCourses</type><college>B</college></request>' \
    'B101' ''

# College C
check "queryCourses - College C" \
    '<request><type>queryCourses</type><college>C</college></request>' \
    'C101' ''

# Invalid college code
check "queryCourses - Invalid college" \
    '<request><type>queryCourses</type><college>X</college></request>' \
    'false' ''

# Missing college field
check "queryCourses - Missing college" \
    '<request><type>queryCourses</type></request>' \
    'false' ''

# ============================================================
# 3. SHARE COURSES (shareCourse) - View courses from other colleges
# ============================================================
echo ""
echo "--- [3/9] Share Courses ---"

# College A views shared courses from B and C
check "shareCourse - College A source" \
    '<request><type>shareCourse</type><source>A</source></request>' \
    'B101' ''

# College B views shared courses from A and C
check "shareCourse - College B source" \
    '<request><type>shareCourse</type><source>B</source></request>' \
    'A101' ''

# College C views shared courses from A and B
check "shareCourse - College C source" \
    '<request><type>shareCourse</type><source>C</source></request>' \
    'A101' ''

# Should NOT include own college's courses
check "shareCourse - College A should not include own courses" \
    '<request><type>shareCourse</type><source>A</source></request>' \
    'success>true<' 'A101'

# ============================================================
# 4. MY COURSES (myCourses) - Before cross-select (baseline)
# ============================================================
echo ""
echo "--- [4/9] My Courses (Baseline) ---"

# A001 should have 5 courses (A101-A105)
check "myCourses - A001 baseline (5 courses)" \
    '<request><type>myCourses</type><college>A</college><studentId>A001</studentId></request>' \
    'A105' ''

# B001 should have 5 courses (B101-B105)
check "myCourses - B001 baseline" \
    '<request><type>myCourses</type><college>B</college><studentId>B001</studentId></request>' \
    'B105' ''

# C001 should have 5 courses
check "myCourses - C001 baseline" \
    '<request><type>myCourses</type><college>C</college><studentId>C001</studentId></request>' \
    'C105' ''

# Invalid student ID
check "myCourses - Invalid student ID" \
    '<request><type>myCourses</type><college>A</college><studentId>INVALID</studentId></request>' \
    'success>true<' ''

# Empty student ID
check "myCourses - Empty student ID" \
    '<request><type>myCourses</type><college>A</college><studentId></studentId></request>' \
    'success>true<' ''

# ============================================================
# 5. CROSS SELECT (crossSelect) - Core functionality
# ============================================================
echo ""
echo "--- [5/9] Cross-College Selection (crossSelect) ---"

# 5a. Select SHARED course from another college (should succeed)
check "crossSelect - A001 selects B103 (shared)" \
    '<request><type>crossSelect</type><studentId>A001</studentId><courseId>B103</courseId></request>' \
    'success>true<'

# 5b. Verify B103 now appears in A001's courses
check "crossSelect verify - B103 in A001 myCourses" \
    '<request><type>myCourses</type><college>A</college><studentId>A001</studentId></request>' \
    'B103' ''

# 5c. Select a course from C college (C101 should be shared)
check "crossSelect - A001 selects C101 (shared)" \
    '<request><type>crossSelect</type><studentId>A001</studentId><courseId>C101</courseId></request>' \
    'success>true<'

# 5d. Verify C101 appears in A001's courses
check "crossSelect verify - C101 in A001 myCourses" \
    '<request><type>myCourses</type><college>A</college><studentId>A001</studentId></request>' \
    'C101' ''

# 5e. B001 selects C103 (shared)
check "crossSelect - B001 selects C103 (shared)" \
    '<request><type>crossSelect</type><studentId>B001</studentId><courseId>C103</courseId></request>' \
    'success>true<'

# 5f. Verify C103 in B001's courses
check "crossSelect verify - C103 in B001 myCourses" \
    '<request><type>myCourses</type><college>B</college><studentId>B001</studentId></request>' \
    'C103' ''

# 5g. C001 selects A101 (shared)
check "crossSelect - C001 selects A101 (shared)" \
    '<request><type>crossSelect</type><studentId>C001</studentId><courseId>A101</courseId></request>' \
    'success>true<'

# 5h. Verify A101 in C001's courses
check "crossSelect verify - A101 in C001 myCourses" \
    '<request><type>myCourses</type><college>C</college><studentId>C001</studentId></request>' \
    'A101' ''

# ============================================================
# 6. CROSS SELECT - NEGATIVE TESTS (should all fail)
# ============================================================
echo ""
echo "--- [6/9] Cross Selection Negative Tests ---"

# 6a. Select non-shared course (should fail)
check "crossSelect reject - non-shared course B102" \
    '<request><type>crossSelect</type><studentId>A001</studentId><courseId>B102</courseId></request>' \
    'false' 'success>true<'

# 6b. Select non-shared course from A
check "crossSelect reject - non-shared course A102" \
    '<request><type>crossSelect</type><studentId>B001</studentId><courseId>A102</courseId></request>' \
    'false' 'success>true<'

# 6c. Select non-shared course from C
check "crossSelect reject - non-shared course C102" \
    '<request><type>crossSelect</type><studentId>A001</studentId><courseId>C102</courseId></request>' \
    'false' 'success>true<'

# 6d. Duplicate selection (B103 already selected by A001)
check "crossSelect reject - duplicate selection B103" \
    '<request><type>crossSelect</type><studentId>A001</studentId><courseId>B103</courseId></request>' \
    'false' 'success>true<'

# 6e. Invalid course ID (non-existent)
check "crossSelect reject - non-existent course XXX" \
    '<request><type>crossSelect</type><studentId>A001</studentId><courseId>XXX</courseId></request>' \
    'false' 'success>true<'

# 6f. Empty student ID
check "crossSelect reject - empty student ID" \
    '<request><type>crossSelect</type><studentId></studentId><courseId>B101</courseId></request>' \
    'false' 'success>true<'

# 6g. Empty course ID
check "crossSelect reject - empty course ID" \
    '<request><type>crossSelect</type><studentId>A001</studentId><courseId></courseId></request>' \
    'false' 'success>true<'

# 6h. Malformed XML (missing type)
check "crossSelect reject - no type field" \
    '<request><studentId>A001</studentId><courseId>B101</courseId></request>' \
    'false' ''

# ============================================================
# 7. STATISTICS
# ============================================================
echo ""
echo "--- [7/9] Statistics ---"

check "statistics" \
    '<request><type>statistics</type></request>' \
    'totalStudents' ''

# Verify all 3 colleges present (XML uses code="A"/"B"/"C")
check "statistics - College A data" \
    '<request><type>statistics</type></request>' \
    '<code>A</code>' ''

check "statistics - College B data" \
    '<request><type>statistics</type></request>' \
    '<code>B</code>' ''

check "statistics - College C data" \
    '<request><type>statistics</type></request>' \
    '<code>C</code>' ''

check "statistics - top courses present" \
    '<request><type>statistics</type></request>' \
    'topCourses' ''

# ============================================================
# 8. DROP COURSE (dropCourse)
# ============================================================
echo ""
echo "--- [8/9] Drop Course ---"

# 8a. Drop the cross-selected B103
check "dropCourse - A001 drops B103" \
    '<request><type>dropCourse</type><studentId>A001</studentId><courseId>B103</courseId></request>' \
    'success>true<'

# 8b. Verify B103 no longer in A001's courses
check "dropCourse verify - B103 removed from A001" \
    '<request><type>myCourses</type><college>A</college><studentId>A001</studentId></request>' \
    'success>true<' 'B103'

# 8c. But C101 should still be there
check "dropCourse verify - C101 still in A001" \
    '<request><type>myCourses</type><college>A</college><studentId>A001</studentId></request>' \
    'C101' ''

# 8d. Drop remaining cross-selected courses
check "dropCourse - A001 drops C101" \
    '<request><type>dropCourse</type><studentId>A001</studentId><courseId>C101</courseId></request>' \
    'success>true<'

check "dropCourse - B001 drops C103" \
    '<request><type>dropCourse</type><studentId>B001</studentId><courseId>C103</courseId></request>' \
    'success>true<'

check "dropCourse - C001 drops A101" \
    '<request><type>dropCourse</type><studentId>C001</studentId><courseId>A101</courseId></request>' \
    'success>true<'

# 8e. Drop non-existent selection (should fail)
check "dropCourse reject - non-existent selection" \
    '<request><type>dropCourse</type><studentId>A001</studentId><courseId>B999</courseId></request>' \
    'false' 'success>true<'

# 8f. Empty student ID
check "dropCourse reject - empty student ID" \
    '<request><type>dropCourse</type><studentId></studentId><courseId>B101</courseId></request>' \
    'false' 'success>true<'

# ============================================================
# 9. UNSUPPORTED OPERATIONS
# ============================================================
echo ""
echo "--- [9/9] Unsupported Operations & Edge Cases ---"

check "unsupported type" \
    '<request><type>hackThePlanet</type></request>' \
    'false' ''

check "empty XML" \
    '' \
    'false' ''

check "no type field" \
    '<request><something>else</something></request>' \
    'false' ''

# GET request (not POST)
echo -n "  Testing GET request (should fail as 405)... "
get_resp=$(curl -s http://localhost:8080/api/xml 2>&1)
if echo "$get_resp" | grep -q "POST"; then
    green "PASS: GET request returns 405"
    PASS=$((PASS + 1))
    RESULTS+=("PASS: GET request returns 405")
else
    red "FAIL: GET request - unexpected response: $get_resp"
    FAIL=$((FAIL + 1))
    RESULTS+=("FAIL: GET request")
fi

# ============================================================
echo ""
echo "========================================"
echo "         TEST SUMMARY"
echo "========================================"
echo "Total: $((PASS + FAIL)) | Passed: $(green "$PASS") | Failed: $(red "$FAIL")"
echo ""

if [ $FAIL -gt 0 ]; then
    echo "Failed tests:"
    for r in "${RESULTS[@]}"; do
        if echo "$r" | grep -q "^FAIL"; then
            red "  $r"
        fi
    done
fi

echo ""
echo "========================================"

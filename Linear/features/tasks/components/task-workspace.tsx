"use client";

import { useDeferredValue, useState } from "react";
import { useButlerData } from "@/features/butler/state/butler-provider";
import { TaskBoard } from "@/features/tasks/components/task-board";
import { TaskList } from "@/features/tasks/components/task-list";
import type { Ticket } from "@/types/ticket";

type QuickFilter = "전체" | "긴급" | "오늘 처리" | "문서" | "검수중";
type ViewMode = "board" | "list";
type SortMode = "priority" | "due" | "recent";

const quickFilters: QuickFilter[] = ["전체", "긴급", "오늘 처리", "문서", "검수중"];
const priorityRank = { Urgent: 0, High: 1, Medium: 2, Low: 3 };

function matchesQuickFilter(ticket: Ticket, filter: QuickFilter) {
  switch (filter) {
    case "긴급":
      return ticket.priority === "Urgent";
    case "오늘 처리":
      return ticket.dueLabel.includes("오늘") || ticket.dueLabel.includes("금일");
    case "문서":
      return ticket.category === "Document";
    case "검수중":
      return ticket.status === "Inspecting";
    default:
      return true;
  }
}

function sortTickets(tickets: Ticket[], sortMode: SortMode) {
  const next = [...tickets];

  next.sort((left, right) => {
    if (sortMode === "priority") {
      return priorityRank[left.priority] - priorityRank[right.priority];
    }

    if (sortMode === "due") {
      return left.dueLabel.localeCompare(right.dueLabel, "ko");
    }

    return right.requestedAt.localeCompare(left.requestedAt, "ko");
  });

  return next;
}

export function TaskWorkspace() {
  const { tickets } = useButlerData();
  const [activeFilter, setActiveFilter] = useState<QuickFilter>("전체");
  const [viewMode, setViewMode] = useState<ViewMode>("board");
  const [sortMode, setSortMode] = useState<SortMode>("priority");
  const [searchQuery, setSearchQuery] = useState("");
  const deferredQuery = useDeferredValue(searchQuery);

  const visibleTickets = sortTickets(
    tickets.filter((ticket) => {
      const matchesFilter = matchesQuickFilter(ticket, activeFilter);
      const normalizedQuery = deferredQuery.trim().toLowerCase();

      if (!normalizedQuery) {
        return matchesFilter;
      }

      const haystack = [
        ticket.id,
        ticket.title,
        ticket.summary,
        ticket.propertyName,
        ticket.unitLabel,
        ticket.tenantName,
        ticket.assignee,
        ...ticket.tags,
      ]
        .join(" ")
        .toLowerCase();

      return matchesFilter && haystack.includes(normalizedQuery);
    }),
    sortMode,
  );

  const openTickets = tickets.filter((ticket) => ticket.status !== "Done");
  const todayActions = tickets.filter((ticket) => ticket.dueLabel.includes("오늘") || ticket.dueLabel.includes("금일"));
  const urgentCount = tickets.filter((ticket) => ticket.priority === "Urgent").length;
  const inspectionCount = tickets.filter((ticket) => ticket.status === "Inspecting").length;

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <p className="eyebrow">Issue operations</p>
          <h1>Tasks</h1>
          <p className="muted">
            Linear 스타일 보드를 Butler 운영 흐름에 맞게 치환한 메인 화면입니다. 수선 요청, 민원, 문서 검토,
            정산 작업을 상태별로 관리합니다.
          </p>
        </div>
        <div className="inline-actions">
          <button className="ghost-button" type="button">
            Import Requests
          </button>
          <button className="primary-button" type="button">
            Create Ticket
          </button>
        </div>
      </header>

      <section className="ticket-meta">
        <article className="metric-card">
          <span className="muted">Open tickets</span>
          <strong>{openTickets.length}</strong>
        </article>
        <article className="metric-card">
          <span className="muted">Due today</span>
          <strong>{todayActions.length}</strong>
        </article>
        <article className="metric-card">
          <span className="muted">Urgent issues</span>
          <strong>{urgentCount}</strong>
        </article>
        <article className="metric-card">
          <span className="muted">Inspection queue</span>
          <strong>{inspectionCount}</strong>
        </article>
      </section>

      <section className="filter-row" aria-label="Task filters">
        <div className="quick-filter-group">
          {quickFilters.map((filter) => (
            <button
              key={filter}
              className={`quick-chip${activeFilter === filter ? " active" : ""}`}
              onClick={() => setActiveFilter(filter)}
              type="button"
            >
              {filter}
            </button>
          ))}
        </div>
        <div className="view-toggle">
          <button
            className={viewMode === "board" ? "active" : ""}
            onClick={() => setViewMode("board")}
            type="button"
          >
            Board
          </button>
          <button
            className={viewMode === "list" ? "active" : ""}
            onClick={() => setViewMode("list")}
            type="button"
          >
            List
          </button>
        </div>
        <select
          aria-label="Sort tasks"
          className="select-input"
          onChange={(event) => setSortMode(event.target.value as SortMode)}
          value={sortMode}
        >
          <option value="priority">우선순위</option>
          <option value="due">마감 기준</option>
          <option value="recent">최근 등록순</option>
        </select>
      </section>

      <section className="filter-row" aria-label="Task search">
        <input
          aria-label="Search visible tasks"
          className="search-box toolbar-search"
          onChange={(event) => setSearchQuery(event.target.value)}
          placeholder="작업 제목, 자산, 담당자, 태그 검색"
          type="search"
          value={searchQuery}
        />
        <div className="input-like">현재 필터: {activeFilter}</div>
        <div className="input-like">표시 결과: {visibleTickets.length}건</div>
      </section>

      {viewMode === "board" ? <TaskBoard tickets={visibleTickets} /> : <TaskList tickets={visibleTickets} />}
    </div>
  );
}

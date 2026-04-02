"use client";

import { useState } from "react";
import { notFound } from "next/navigation";
import { useButlerData } from "@/features/butler/state/butler-provider";
import { priorityClassName } from "@/lib/format";
import { ticketColumns } from "@/lib/mock/tickets";
import type { TicketStatus } from "@/types/ticket";

export function TaskDetail({ ticketId }: { ticketId: string }) {
  const { ticketDetails, updateTicketStatus, toggleChecklistItem, addTicketComment, addTicketNote } =
    useButlerData();
  const [commentDraft, setCommentDraft] = useState("");
  const [noteDraft, setNoteDraft] = useState("");
  const ticket = ticketDetails[ticketId];

  if (!ticket) {
    notFound();
  }

  function submitComment() {
    const next = commentDraft.trim();
    if (!next) return;

    addTicketComment(ticketId, next);
    setCommentDraft("");
  }

  function submitNote() {
    const next = noteDraft.trim();
    if (!next) return;

    addTicketNote(ticketId, next);
    setNoteDraft("");
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div className="task-headline">
          <p className="eyebrow">{ticket.id}</p>
          <h1>{ticket.title}</h1>
          <p className="muted">{ticket.summary}</p>
        </div>
        <div className="inline-actions">
          <span className={`pill ${priorityClassName(ticket.priority)}`}>{ticket.priority}</span>
          <span className="pill status-chip">{ticket.status}</span>
          <select
            aria-label="Update ticket status"
            className="select-input"
            onChange={(event) => updateTicketStatus(ticketId, event.target.value as TicketStatus)}
            value={ticket.status}
          >
            {ticketColumns.map((item) => (
              <option key={item} value={item}>
                {item}
              </option>
            ))}
          </select>
        </div>
      </header>

      <div className="detail-layout">
        <div className="detail-main">
          <section className="detail-block">
            <h3>작업 설명</h3>
            <p>{ticket.description}</p>
          </section>

          <section className="detail-block">
            <h3>증빙 및 체크리스트</h3>
            <div className="activity-list">
              {ticket.evidence.map((item) => (
                <article key={item}>{item}</article>
              ))}
            </div>
            <div className="checklist" style={{ marginTop: 14 }}>
              {ticket.checklist.map((item) => (
                <button
                  className={`check-item${item.done ? " done" : ""}`}
                  key={item.id}
                  onClick={() => toggleChecklistItem(ticketId, item.id)}
                  type="button"
                >
                  {item.done ? "완료" : "대기"} · {item.label}
                </button>
              ))}
            </div>
          </section>

          <section className="detail-block">
            <h3>댓글 추가</h3>
            <div className="composer">
              <textarea
                onChange={(event) => setCommentDraft(event.target.value)}
                placeholder="세입자 응답, 임대인 보고, 업체 커뮤니케이션 요약을 남기세요."
                value={commentDraft}
              />
              <div className="button-row">
                <button className="primary-button" onClick={submitComment} type="button">
                  댓글 등록
                </button>
              </div>
            </div>
          </section>

          <section className="detail-block">
            <h3>활동 로그</h3>
            <div className="activity-list">
              {ticket.activities.map((activity) => (
                <article key={activity.id}>
                  <strong>{activity.actor}</strong>
                  <p>{activity.action}</p>
                  <div className="meta-text">{activity.timestamp}</div>
                </article>
              ))}
            </div>
          </section>
        </div>

        <aside className="detail-side">
          <section className="detail-block">
            <h3>작업 메타</h3>
            <div className="detail-list">
              <div>
                <span className="muted">자산</span>
                <strong>{ticket.propertyName}</strong>
              </div>
              <div>
                <span className="muted">호실</span>
                <strong>{ticket.unitLabel}</strong>
              </div>
              <div>
                <span className="muted">세입자</span>
                <strong>{ticket.tenantName}</strong>
              </div>
              <div>
                <span className="muted">담당자</span>
                <strong>{ticket.assignee}</strong>
              </div>
              <div>
                <span className="muted">예상 비용</span>
                <strong>{ticket.estimatedCost}</strong>
              </div>
              <div>
                <span className="muted">실제 비용</span>
                <strong>{ticket.actualCost ?? "미정"}</strong>
              </div>
            </div>
          </section>

          <section className="detail-block">
            <h3>내부 메모</h3>
            <div className="composer" style={{ marginBottom: 14 }}>
              <textarea
                onChange={(event) => setNoteDraft(event.target.value)}
                placeholder="임대인 승인 조건, 분쟁 리스크, 재방문 주의사항을 기록하세요."
                value={noteDraft}
              />
              <div className="button-row">
                <button className="ghost-button" onClick={submitNote} type="button">
                  메모 추가
                </button>
              </div>
            </div>
            <div className="activity-list">
              {ticket.internalNotes.map((note, index) => (
                <article key={`${note}-${index}`}>{note}</article>
              ))}
            </div>
          </section>
        </aside>
      </div>
    </div>
  );
}

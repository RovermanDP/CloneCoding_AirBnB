"use client";

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { useButlerData } from "@/features/butler/state/butler-provider";
import type { TriageRequest, TriageRequestStatus } from "@/types/triage";

const triageFilters: TriageRequestStatus[] = ["New", "On Hold", "Rejected", "Converted"];

export function TriageWorkspace() {
  const router = useRouter();
  const { triageRequests, convertRequestToTicket, setTriageRequestStatus } = useButlerData();
  const [activeStatus, setActiveStatus] = useState<TriageRequestStatus>("New");
  const [selectedId, setSelectedId] = useState<string>(triageRequests[0]?.id ?? "");

  const visibleRequests = useMemo(
    () => triageRequests.filter((request) => request.status === activeStatus),
    [activeStatus, triageRequests],
  );

  const selectedRequest =
    visibleRequests.find((request) => request.id === selectedId) ?? visibleRequests[0] ?? null;

  function selectRequest(request: TriageRequest) {
    setSelectedId(request.id);
  }

  function updateStatus(status: TriageRequestStatus) {
    if (!selectedRequest) return;
    setTriageRequestStatus(selectedRequest.id, status);
  }

  function createTicket() {
    if (!selectedRequest) return;
    const ticketId = convertRequestToTicket(selectedRequest.id);
    if (ticketId) {
      router.push(`/tasks/${ticketId}`);
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <p className="eyebrow">Inbound requests</p>
          <h1>Triage</h1>
          <p className="muted">신규 민원, 문서 업로드, 정산 문의를 작업 티켓으로 전환하는 운영 허브입니다.</p>
        </div>
      </header>

      <section className="filter-row" aria-label="Triage filters">
        <div className="quick-filter-group">
          {triageFilters.map((filter) => (
            <button
              className={`quick-chip${activeStatus === filter ? " active" : ""}`}
              key={filter}
              onClick={() => setActiveStatus(filter)}
              type="button"
            >
              {filter}
            </button>
          ))}
        </div>
        <div className="input-like">Visible requests: {visibleRequests.length}</div>
        <div className="input-like">Selected: {selectedRequest?.id ?? "None"}</div>
      </section>

      <div className="detail-layout">
        <section className="detail-main">
          <div className="stack">
            {visibleRequests.length === 0 ? (
              <div className="empty-state">현재 상태에 해당하는 요청이 없습니다.</div>
            ) : (
              visibleRequests.map((request) => (
                <button
                  className={`task-card${selectedRequest?.id === request.id ? " active-card" : ""}`}
                  key={request.id}
                  onClick={() => selectRequest(request)}
                  style={{ textAlign: "left", cursor: "pointer", width: "100%" }}
                  type="button"
                >
                  <div className="pill-row">
                    <span className="pill">{request.status}</span>
                    <span className="pill status-chip">{request.category}</span>
                  </div>
                  <h3>{request.title}</h3>
                  <p>{request.summary}</p>
                  <div className="task-footer">
                    <span>{request.propertyName}</span>
                    <span>{request.unitLabel}</span>
                  </div>
                  <div className="task-footer">
                    <span>{request.tenantName}</span>
                    <span>{request.receivedAt}</span>
                  </div>
                </button>
              ))
            )}
          </div>
        </section>

        <aside className="detail-side">
          {selectedRequest ? (
            <section className="detail-block">
              <h3>{selectedRequest.id}</h3>
              <p>{selectedRequest.summary}</p>
              <div className="detail-list" style={{ marginTop: 16 }}>
                <div>
                  <span className="muted">Property</span>
                  <strong>{selectedRequest.propertyName}</strong>
                </div>
                <div>
                  <span className="muted">Tenant</span>
                  <strong>{selectedRequest.tenantName}</strong>
                </div>
                <div>
                  <span className="muted">Attachments</span>
                  <strong>{selectedRequest.attachments}</strong>
                </div>
                <div>
                  <span className="muted">Suggested priority</span>
                  <strong>{selectedRequest.suggestedPriority}</strong>
                </div>
              </div>

              <div className="activity-list" style={{ marginTop: 16 }}>
                {selectedRequest.tags.map((tag) => (
                  <article key={tag}>Tag · {tag}</article>
                ))}
              </div>

              <div className="button-row" style={{ justifyContent: "flex-start", marginTop: 18, flexWrap: "wrap" }}>
                <button className="primary-button" onClick={createTicket} type="button">
                  Create ticket
                </button>
                <button className="ghost-button" onClick={() => updateStatus("On Hold")} type="button">
                  Hold
                </button>
                <button className="ghost-button" onClick={() => updateStatus("Rejected")} type="button">
                  Reject
                </button>
                <button className="ghost-button" onClick={() => updateStatus("New")} type="button">
                  Reset
                </button>
              </div>
            </section>
          ) : (
            <div className="empty-state">선택된 요청이 없습니다.</div>
          )}
        </aside>
      </div>
    </div>
  );
}

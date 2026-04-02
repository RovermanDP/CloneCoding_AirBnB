"use client";

import Link from "next/link";
import { useDeferredValue, useState } from "react";
import { useButlerData } from "@/features/butler/state/butler-provider";

type PropertySort = "risk" | "tickets" | "units";

export function PropertiesWorkspace() {
  const { properties, tickets } = useButlerData();
  const [query, setQuery] = useState("");
  const [sortBy, setSortBy] = useState<PropertySort>("risk");
  const deferredQuery = useDeferredValue(query);

  const portfolio = [...properties]
    .map((property) => {
      const propertyTickets = tickets.filter((ticket) => ticket.propertyName === property.name);
      const openTickets = propertyTickets.filter((ticket) => ticket.status !== "Done");
      const urgentTickets = openTickets.filter((ticket) => ticket.priority === "Urgent");

      return {
        ...property,
        openTickets,
        urgentTickets,
        reviewCount: openTickets.filter(
          (ticket) => ticket.status === "Inspecting" || ticket.status === "Reviewing",
        ).length,
      };
    })
    .filter((property) => {
      const normalized = deferredQuery.trim().toLowerCase();
      if (!normalized) return true;

      return [property.name, property.address].join(" ").toLowerCase().includes(normalized);
    })
    .sort((left, right) => {
      if (sortBy === "tickets") {
        return right.openTickets.length - left.openTickets.length;
      }
      if (sortBy === "units") {
        return right.units - left.units;
      }
      return right.riskScore - left.riskScore;
    });

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <p className="eyebrow">Portfolio view</p>
          <h1>Properties</h1>
          <p className="muted">자산별 운영 현황, 활성 티켓, 리스크 지표를 연결해 보는 포트폴리오 화면입니다.</p>
        </div>
      </header>

      <section className="filter-row" aria-label="Property filters">
        <input
          aria-label="Search properties"
          className="search-box toolbar-search"
          onChange={(event) => setQuery(event.target.value)}
          placeholder="건물명 또는 주소 검색"
          type="search"
          value={query}
        />
        <select
          aria-label="Sort properties"
          className="select-input"
          onChange={(event) => setSortBy(event.target.value as PropertySort)}
          value={sortBy}
        >
          <option value="risk">리스크 순</option>
          <option value="tickets">활성 티켓 순</option>
          <option value="units">호실 수 순</option>
        </select>
        <div className="input-like">표시 자산: {portfolio.length}</div>
      </section>

      <section className="placeholder-grid">
        {portfolio.map((property) => (
          <Link key={property.id} className="placeholder-card" href={`/properties/${property.id}`}>
            <div className="link-row">
              <h3>{property.name}</h3>
              <span className="pill status-chip">Risk {property.riskScore}</span>
            </div>
            <p>{property.address}</p>

            <div className="stats-grid" style={{ marginTop: 16 }}>
              <div className="mini-stat">
                <span className="muted">호실 수</span>
                <strong>{property.units}</strong>
              </div>
              <div className="mini-stat">
                <span className="muted">활성 티켓</span>
                <strong>{property.openTickets.length}</strong>
              </div>
              <div className="mini-stat">
                <span className="muted">긴급 건수</span>
                <strong>{property.urgentTickets.length}</strong>
              </div>
              <div className="mini-stat">
                <span className="muted">검토/검수</span>
                <strong>{property.reviewCount}</strong>
              </div>
            </div>

            <ul className="placeholder-list">
              <li>월간 비용 · {property.monthlySpend}</li>
              <li>최근 작업 · {property.openTickets[0]?.title ?? "진행 중인 작업 없음"}</li>
            </ul>
          </Link>
        ))}
      </section>
    </div>
  );
}


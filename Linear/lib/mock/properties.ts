import type { Property } from "@/types/property";

export const mockProperties: Property[] = [
  {
    id: "prop-seongsu",
    name: "Seongsu Riverside",
    address: "서울 성동구 성수동 2가 315-11",
    units: 42,
    activeTickets: 4,
    monthlySpend: "₩3.6M",
    riskScore: 74
  },
  {
    id: "prop-mapo",
    name: "Mapo Hillstate",
    address: "서울 마포구 창전동 13-5",
    units: 28,
    activeTickets: 2,
    monthlySpend: "₩1.9M",
    riskScore: 58
  },
  {
    id: "prop-ilsan",
    name: "Ilsan Central View",
    address: "경기 고양시 일산동구 장항동 887",
    units: 31,
    activeTickets: 6,
    monthlySpend: "₩4.1M",
    riskScore: 81
  }
];


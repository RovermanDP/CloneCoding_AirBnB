import type { Ticket, TicketDetail, TicketStatus } from "@/types/ticket";

export const ticketColumns: TicketStatus[] = [
  "Received",
  "Reviewing",
  "Assigned",
  "Working",
  "Inspecting",
  "Done"
];

export const mockTickets: Ticket[] = [
  {
    id: "BT-201",
    title: "402호 욕실 천장 누수 점검",
    summary: "세입자가 천장 누수 흔적과 냄새를 신고했습니다.",
    propertyName: "Seongsu Riverside",
    unitLabel: "402호",
    tenantName: "김민지",
    assignee: "이서준",
    priority: "Urgent",
    status: "Received",
    requestedAt: "오늘 09:20",
    dueLabel: "오늘 중 1차 연락",
    estimatedCost: "₩400,000",
    category: "Repair",
    tags: ["누수", "사진 3장", "긴급"]
  },
  {
    id: "BT-197",
    title: "관리비 청구서 PDF 업로드 검토",
    summary: "OCR 파싱 전 청구 항목 누락 여부를 확인해야 합니다.",
    propertyName: "Mapo Hillstate",
    unitLabel: "1203호",
    tenantName: "박정우",
    assignee: "최다은",
    priority: "Medium",
    status: "Reviewing",
    requestedAt: "오늘 08:15",
    dueLabel: "내일 오전",
    estimatedCost: "₩0",
    category: "Document",
    tags: ["OCR", "청구서"]
  },
  {
    id: "BT-188",
    title: "현관 도어락 배터리 경고 교체",
    summary: "입주민이 출입 오류 가능성을 신고했습니다.",
    propertyName: "Ilsan Central View",
    unitLabel: "807호",
    tenantName: "정유나",
    assignee: "정현우",
    priority: "High",
    status: "Assigned",
    requestedAt: "어제 17:40",
    dueLabel: "오늘 오후",
    estimatedCost: "₩80,000",
    category: "Repair",
    tags: ["도어락", "현장방문"]
  },
  {
    id: "BT-176",
    title: "엘리베이터 소음 민원 현장 확인",
    summary: "야간 소음 민원 재발로 관리사무소 협의가 필요합니다.",
    propertyName: "Seongsu Riverside",
    unitLabel: "공용부",
    tenantName: "관리실 접수",
    assignee: "이서준",
    priority: "High",
    status: "Working",
    requestedAt: "어제 15:05",
    dueLabel: "금일 18:00",
    estimatedCost: "₩250,000",
    actualCost: "₩180,000",
    category: "Complaint",
    tags: ["소음", "관리실"]
  },
  {
    id: "BT-170",
    title: "보수 완료 사진 검수 및 임대인 보고",
    summary: "싱크대 수전 교체 건 완료 보고서 작성 단계입니다.",
    propertyName: "Mapo Hillstate",
    unitLabel: "604호",
    tenantName: "오수빈",
    assignee: "최다은",
    priority: "Medium",
    status: "Inspecting",
    requestedAt: "어제 10:30",
    dueLabel: "오늘 17:00",
    estimatedCost: "₩120,000",
    actualCost: "₩115,000",
    category: "Repair",
    tags: ["검수", "보고"]
  },
  {
    id: "BT-154",
    title: "전월 미납 관리비 정산 완료",
    summary: "정산서 발행 및 세입자 확인 절차가 종료되었습니다.",
    propertyName: "Ilsan Central View",
    unitLabel: "1502호",
    tenantName: "서민호",
    assignee: "강예린",
    priority: "Low",
    status: "Done",
    requestedAt: "3월 30일",
    dueLabel: "완료",
    estimatedCost: "₩0",
    actualCost: "₩0",
    category: "Settlement",
    tags: ["정산", "완료"]
  }
];

export const mockTicketDetails: Record<string, TicketDetail> = {
  "BT-201": {
    ...mockTickets[0],
    description:
      "세입자 제출 사진에서 욕실 천장 모서리 쪽 누수 흔적이 반복적으로 확인됩니다. 누수 원인이 상층 배관인지 외벽 결로인지 1차 판별이 필요합니다.",
    evidence: ["누수 사진 3건 업로드", "세입자 음성 메모 1건", "3월 동일 민원 이력 존재"],
    checklist: [
      { id: "c1", label: "세입자 1차 응답 및 방문 가능 시간 확보", done: true },
      { id: "c2", label: "협력 업체 배정 및 예상 견적 수집", done: false },
      { id: "c3", label: "임대인 사전 승인 요청", done: false }
    ],
    activities: [
      { id: "a1", actor: "시스템", action: "긴급 민원으로 자동 분류", timestamp: "오늘 09:20" },
      { id: "a2", actor: "김민지", action: "현장 사진 3장을 업로드", timestamp: "오늘 09:18" },
      { id: "a3", actor: "이서준", action: "담당자 임시 배정", timestamp: "오늘 09:24" }
    ],
    internalNotes: [
      "직전 유사 건에서 상층 배관 누수로 판정된 이력이 있음.",
      "임대인은 30만원 초과 시 사전 승인 필요."
    ]
  },
  "BT-188": {
    ...mockTickets[2],
    description:
      "현관문 도어락에서 저전력 경고음이 반복되어 입주민 출입 불편 우려가 있습니다. 업체 방문 전 자가 교체 가능 여부도 함께 확인합니다.",
    evidence: ["기기 모델명 수집 완료", "입주민 영상 1건"],
    checklist: [
      { id: "c4", label: "방문 시간 조율", done: true },
      { id: "c5", label: "예비 배터리 재고 확인", done: true },
      { id: "c6", label: "작업 완료 후 테스트 영상 수집", done: false }
    ],
    activities: [
      { id: "a4", actor: "정현우", action: "현장 방문 일정 등록", timestamp: "오늘 10:40" },
      { id: "a5", actor: "시스템", action: "도어락 카테고리 자동 태깅", timestamp: "오늘 10:10" }
    ],
    internalNotes: ["같은 단지 동일 모델 교체 이력 있음."]
  }
};

export function buildTicketDetail(ticket: Ticket): TicketDetail {
  return {
    ...ticket,
    description: ticket.summary,
    evidence: [`요청 접수 시 첨부 ${ticket.tags.length}개 태그 기록`],
    checklist: [
      { id: `${ticket.id}-c1`, label: "요청 내용 확인", done: true },
      { id: `${ticket.id}-c2`, label: "담당자 배정", done: ticket.status !== "Received" },
      { id: `${ticket.id}-c3`, label: "결과 보고서 작성", done: ticket.status === "Done" },
    ],
    activities: [
      {
        id: `${ticket.id}-a1`,
        actor: "시스템",
        action: `${ticket.status} 상태의 기본 티켓이 생성되었습니다.`,
        timestamp: ticket.requestedAt,
      },
    ],
    internalNotes: ["초기 mock 데이터에서 생성된 티켓입니다."],
  };
}

export function getTicketById(ticketId: string) {
  const seeded = mockTicketDetails[ticketId];
  if (seeded) return seeded;

  const ticket = mockTickets.find((item) => item.id === ticketId);
  return ticket ? buildTicketDetail(ticket) : null;
}

function SlotSection({
  title,
  slots,
  selectedDate,
  origDate,
  origTime,
  toDateStr,
  formatTime,
  isPastSlot,
  onSlotClick,
}) {

  if (slots.length === 0) {
    return null;
  }

  const selectedDateStr = toDateStr(selectedDate);

  return (
    <div className="slots-section">
      <div className="slots-section-label">{title}</div>
      <div className="slots-grid">
        {slots.map(s => {
          const isFull = s.remaining <= 0;
          const past = isPastSlot(selectedDateStr, s.time);
          const disabled = isFull || past;
          const isOrig = origTime && origDate === selectedDateStr && s.time === origTime;

          return (
            <button
              key={s.time}
              className={`slot-chip ${disabled ? 'disabled' : ''} ${isFull ? 'full' : ''} ${isOrig ? 'slot-original' : ''}`}
              disabled={disabled}
              onClick={() => onSlotClick(s.time)}
            >
              {formatTime(s.time)}
              {isOrig && <span className="slot-original-label">current</span>}
            </button>
          );
        })}
      </div>
    </div>
  );
}

export function SlotList({
  slots,
  slotsLoading,
  selectedDate,
  morningSlots,
  afternoonSlots,
  origDate,
  origTime,
  toDateStr,
  formatDateLong,
  formatTime,
  isPastSlot,
  onSlotClick,
}) {

  return (
    <div className="slots-panel">
      <div className="slots-panel-header">
        <h3>Available Slots - {formatDateLong(toDateStr(selectedDate))}</h3>
      </div>

      {slotsLoading && <div className="slots-loading">Loading available slots...</div>}

      {!slotsLoading && slots.length === 0 && (
        <div className="empty-state">
          <p>No slots available for this date.</p>
        </div>
      )}

      {!slotsLoading && (
        <>
          <SlotSection
            title="Morning"
            slots={morningSlots}
            selectedDate={selectedDate}
            origDate={origDate}
            origTime={origTime}
            toDateStr={toDateStr}
            formatTime={formatTime}
            isPastSlot={isPastSlot}
            onSlotClick={onSlotClick}
          />
          <SlotSection
            title="Afternoon"
            slots={afternoonSlots}
            selectedDate={selectedDate}
            origDate={origDate}
            origTime={origTime}
            toDateStr={toDateStr}
            formatTime={formatTime}
            isPastSlot={isPastSlot}
            onSlotClick={onSlotClick}
          />
        </>
      )}
    </div>
  );
  
}
